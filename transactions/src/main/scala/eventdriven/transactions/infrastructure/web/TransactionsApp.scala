package eventdriven.transactions.infrastructure.web

import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{KafkaConsumerConfig, KafkaProducerConfig}
import eventdriven.core.infrastructure.messaging.kafka.{KafkaEventListener, KafkaEventProducer}
import eventdriven.core.infrastructure.store.CacheInMem
import eventdriven.transactions.domain.event.payment.{PaymentEvent, PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.domain.event.transaction.{TransactionDecisioned, TransactionEvent}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.usecase.{GetAccountSummary, ProcessAccountChangeEvents, ProcessPaymentEvent, ProcessTransaction}
import eventdriven.transactions.infrastructure.messaging.Topic
import eventdriven.transactions.infrastructure.store.{AccountInfoStoreInMemory, TransactionStoreInMemory}
import eventdriven.transactions.infrastructure.web.serde.{ErrorResponseSerde, GetAccountSummarySerde, OkResponseSerde, ProcessTransactionSerde}

import scala.collection.mutable
import wvlet.log.LogSupport

import java.time.Instant
import cats.effect._
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server._
import cats.syntax.all._
import eventdriven.transactions.domain.event.account.AccountCreditLimitUpdated

import scala.concurrent.ExecutionContext

object TransactionsApp extends IOApp.Simple with LogSupport {
  val esData = mutable.ListBuffer[TransactionEvent]()
  esData.append(TransactionDecisioned(123, 12345678, "1", 1000, "Approved", "", "1", 1001))
  esData.append(TransactionDecisioned(123, 12345678, "2", 1099, "Approved", "", "1", 1002))
  esData.append(TransactionDecisioned(123, 12345678, "3", 2100, "Approved", "", "1", 1003))
  val es = new TransactionStoreInMemory(esData)

  val accountInfoData = mutable.ListBuffer[AccountInfo]()
  accountInfoData.append(AccountInfo(123, 12345678, 50000, "80126", "CO"))
  val accountInfoStore = new AccountInfoStoreInMemory(accountInfoData)

  val paymentCache = new CacheInMem[PaymentEvent]

  val kconfig = KafkaProducerConfig(
    "localhost",
    19092,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  val dispatcher = new KafkaEventProducer("transactions", kconfig)

  val kconfigConsumer = KafkaConsumerConfig(
    "localhost",
    19092,
    "group1",
    "org.apache.kafka.common.serialization.StringDeserializer",
    "org.apache.kafka.common.serialization.StringDeserializer")

  // NOTE: we would also have here account events consumer but skipping for now to keep things simple
  val accountCreditLimitUpdated = new KafkaEventListener(Topic.AccountCreditLimitUpdated.toString, "transactions", kconfigConsumer)
  val paymentSubmittedConsumer = new KafkaEventListener(Topic.PaymentSubmitted.toString, "transactions", kconfigConsumer)
  val paymentReturnedConsumer = new KafkaEventListener(Topic.PaymentReturned.toString, "transactions", kconfigConsumer)

  implicit val executor = ExecutionContext.global

  val accountCreditLimitUpdatedConsumerIO = IO.interruptible {
    info("Listening to accountCreditLimitUpdated")
    while(true) {
      accountCreditLimitUpdated.take match {
        case Some(xs) => xs.foreach { json =>
          (for {
            accountEvent <- AccountCreditLimitUpdated.fromJson(json)
            _ = info(accountEvent)
            _ <- ProcessAccountChangeEvents(accountEvent)(accountInfoStore)
          } yield ()) match {
            case Left(err) => error(err.getMessage)
            case Right(_) => info(s"Event from ${Topic.AccountCreditLimitUpdated.toString} processes successfully")
          }
        }
        case None => ()
      }
    }
  }

  val paymentSubmittedConsumerIO = IO.interruptible {
    info("Listening to paymentSubmitted")
    while(true) {
      paymentSubmittedConsumer.take match {
        case Some(xs) => xs.foreach { json =>
          (for {
            payment <- PaymentSubmitted.fromJson(json)
            result <- ProcessPaymentEvent(payment)(es, dispatcher)
            _ = info(result)
          } yield ()) match {
            case Left(err) => error(err.getMessage)
            case Right(_) => info(s"Event from ${Topic.PaymentSubmitted.toString} processes successfully")
          }
        }
        case None => ()
      }
    }
  }

  val paymentReturnedConsumerIO = IO.interruptible {
    info("Listening to paymentReturned")
    while(true) {
      paymentReturnedConsumer.take match {
        case Some(xs) => xs.foreach { json =>
          (for {
            payment <- PaymentReturned.fromJson(json)
            result <- ProcessPaymentEvent(payment)(es, dispatcher)
            _ = info(result)
          } yield ()) match {
            case Left(err) => error(err.getMessage)
            case Right(_) => info(s"Event from ${Topic.PaymentReturned.toString} processes successfully")
          }
        }
        case None => ()
      }
    }
  }

  val routes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "process-purchase-transaction" => {

      val logic = (body: String) => IO.interruptible {
        (for {
          preAuth <- ProcessTransactionSerde.fromJson(body)
          processed <- ProcessTransaction(preAuth)(es, accountInfoStore, dispatcher)
        } yield processed) match {
          case Left(err) => {
            err.printStackTrace()
            Ok(ErrorResponseSerde.toJson(err.getMessage))
          }
          case Right(resp) => Ok(ProcessTransactionSerde.toJson(resp))
        }
      }

      req.as[String].flatMap(logic).flatten
    }

    case GET -> Root / "account-summary" / accountIdString => {
      val accountId = Integer.parseInt(accountIdString)
      GetAccountSummary(accountId)(es, accountInfoStore) match {
        case Right(obj) => Ok(GetAccountSummarySerde.toJson(obj))
        case Left(err) => {
          err.printStackTrace()
          Ok(ErrorResponseSerde.toJson(err.getMessage))
        }
      }
    }
  }.orNotFound

  def run: IO[Unit] = {
    val app = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

    val listOfIos = List(app, accountCreditLimitUpdatedConsumerIO, paymentSubmittedConsumerIO, paymentReturnedConsumerIO)

    listOfIos.parSequence_
  }
}
