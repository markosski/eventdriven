package eventdriven.transactions.infrastructure.web

import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{KafkaConsumerConfig, KafkaProducerConfig}
import eventdriven.core.infrastructure.messaging.kafka.{KafkaEventListener, KafkaEventProducer}
import wvlet.log.LogSupport
import cats.effect._
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server._
import cats.syntax.all._
import eventdriven.core.infrastructure.messaging.Topics
import eventdriven.core.domain.events.{AccountCreditLimitUpdatedEvent, PaymentReturnedEvent, PaymentSubmittedEvent}
import eventdriven.core.util.json
import eventdriven.transactions.domain.model.transaction.{DecisionedTransactionResponse, PreDecisionedTransactionRequest, TransactionInfoResponse}
import eventdriven.transactions.infrastructure.env.local
import eventdriven.transactions.infrastructure.web.serde.ErrorResponseSerde
import eventdriven.transactions.usecase.{GetAccountSummary, GetRecentTransactions, ProcessAccountChangeEvents, ProcessPaymentEvent, ProcessTransaction}

object TransactionsApp extends IOApp.Simple with LogSupport {
  val environment = local.getEnv

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

  val accountCreditLimitUpdated = new KafkaEventListener(Topics.AccountCreditLimitUpdatedV1.toString, "transactions", kconfigConsumer)
  val paymentSubmittedConsumer = new KafkaEventListener(Topics.PaymentSubmittedV1.toString, "transactions", kconfigConsumer)
  val paymentReturnedConsumer = new KafkaEventListener(Topics.PaymentReturnedV1.toString, "transactions", kconfigConsumer)

  val accountCreditLimitUpdatedConsumerIO = IO.interruptible {
    info("Listening to accountCreditLimitUpdated")
    while(true) {
      accountCreditLimitUpdated.take match {
        case Some(xs) => xs.foreach { json =>
          (for {
            accountEvent <- AccountCreditLimitUpdatedEvent.fromJson(json)
            _ = info(s"Processing event accountCreditLimitUpdated, payload: $accountEvent")
            _ <- ProcessAccountChangeEvents(accountEvent)(environment.accountInfoStore)
          } yield ()) match {
            case Left(err) => error(err.getMessage)
            case Right(_) => info(s"Event from ${Topics.AccountCreditLimitUpdatedV1.toString} processed successfully")
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
            payment <- PaymentSubmittedEvent.fromJson(json)
            _ = info(s"Processing event paymentSubmitted, payload: $payment")
            result <- ProcessPaymentEvent(payment)(environment.transactionStore, dispatcher)
            _ = info(result)
          } yield ()) match {
            case Left(err) => error(err.getMessage)
            case Right(_) => info(s"Event from ${Topics.PaymentSubmittedV1.toString} processed successfully")
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
            payment <- PaymentReturnedEvent.fromJson(json)
            _ = info(s"Processing event paymentReturned, payload: $payment")
            result <- ProcessPaymentEvent(payment)(environment.transactionStore, dispatcher)
            _ = info(result)
          } yield ()) match {
            case Left(err) => error(err.getMessage)
            case Right(_) => info(s"Event from ${Topics.PaymentReturnedV1.toString} processed successfully")
          }
        }
        case None => ()
      }
    }
  }

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "_health" => Ok(s"""{"response": "healthy"}""")
    case req @ POST -> Root / "process-purchase-transaction" => {
      val logic = (body: String) => IO.interruptible {
        (for {
          preAuth <- PreDecisionedTransactionRequest.fromJson(body)
          _ = info(s"Received process transaction request: $preAuth")
          processed <- ProcessTransaction(preAuth)(environment.transactionStore, environment.accountInfoStore, dispatcher)
        } yield processed) match {
          case Left(err) => {
            err.printStackTrace()
            Ok(ErrorResponseSerde.toJson(err.getMessage))
          }
          case Right(resp) => {
            info(s"Process transaction response: $resp")
            Ok(DecisionedTransactionResponse.toJson(resp))
          }
        }
      }
      req.as[String].flatMap(logic).flatten
    }

    case GET -> Root / "balance" / accountIdString => {
      info(s"Received account summary request for account $accountIdString")
      val accountId = Integer.parseInt(accountIdString)
      GetAccountSummary(accountId)(environment.transactionStore, environment.accountInfoStore) match {
        case Right(resp) => {
          info(s"Account summary transaction response: $resp")
          Ok(json.anyToJson(resp))
        }
        case Left(err) => {
          err.printStackTrace()
          Ok(ErrorResponseSerde.toJson(err.getMessage))
        }
      }
    }

    case GET -> Root / "transactions" / accountIdString => {
      info(s"Received recent transactions request for account $accountIdString")
      val accountId = Integer.parseInt(accountIdString)
      GetRecentTransactions(accountId)(environment.transactionStore) match {
        case Right(resp) => {
          info(s"Account recent transactions response: $resp")
          Ok(TransactionInfoResponse.toJson(resp))
        }
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
