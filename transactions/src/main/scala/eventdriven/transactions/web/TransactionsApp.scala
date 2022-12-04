package eventdriven.transactions.web

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
import eventdriven.transactions.infrastructure.AppConfig
import eventdriven.transactions.infrastructure.env.local
import eventdriven.transactions.usecase.{AuthorizeTransaction, ClearTransactions, GetAccountSummary, GetRecentTransactions, ProcessAccountChangeEvents, ProcessPaymentEvent}
import eventdriven.transactions.web.serde.{ErrorResponseSerde, PreDecisionedTransactionRequestSerde, TransactionToClearSerde}
import pureconfig._
import pureconfig.generic.auto._

object TransactionsApp extends IOApp.Simple with LogSupport {
  val config = ConfigSource.default.load[AppConfig] match {
    case Left(err) => throw new Exception(err.toString())
    case Right(config) => config
  }
  val environment = local.getEnv

  val kconfig = KafkaProducerConfig(
    config.kafkaConfig.host,
    config.kafkaConfig.port,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  val dispatcher = new KafkaEventProducer("transactions", kconfig)

  val kconfigConsumer = KafkaConsumerConfig(
    config.kafkaConfig.host,
    config.kafkaConfig.port,
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

    case req @ POST -> Root / "authorize" => {
      val logic = (body: String) => {
        (for {
          preAuth <- PreDecisionedTransactionRequestSerde.fromJson(body)
          _ = info(s"Received process transaction request: $preAuth")
          processed <- AuthorizeTransaction(preAuth)(environment.transactionStore, environment.accountInfoStore, dispatcher)
        } yield processed) match {
          case Left(err) => {
            err.printStackTrace()
            Ok(ErrorResponseSerde.toJson(err.getMessage))
          }
          case Right(resp) => {
            info(s"Process transaction response: $resp")
            Ok(json.anyToJson(resp))
          }
        }
      }
      req.as[String].map(logic).flatten
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
          Ok(json.anyToJson(resp))
        }
        case Left(err) => {
          err.printStackTrace()
          Ok(ErrorResponseSerde.toJson(err.getMessage))
        }
      }
    }

    case req @ POST -> Root / "clearAuths" => {
      info(s"Received clearing request")
      val logic = (body: String) => {
        (for {
          input <- TransactionToClearSerde.fromJson(body)
          result = ClearTransactions(input)(environment.transactionStore, dispatcher)
        } yield result) match {
          case Left(err) => {
            err.printStackTrace()
            Ok(ErrorResponseSerde.toJson(err.getMessage))
          }
          case Right(resp) => {
            val withSimplifiedError = resp.map(_.fold(err => err.getMessage, x => x))
            info(s"Process transaction response: $withSimplifiedError")
            Ok(json.anyToJson(withSimplifiedError))
          }
        }
      }
      req.as[String].map(logic).flatten
    }

  }.orNotFound

  def run: IO[Unit] = {
    val app = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(config.webConfig.port).getOrElse(port"0"))
      .withHttpApp(routes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

    val listOfIos = List(
      app,
      accountCreditLimitUpdatedConsumerIO,
      paymentSubmittedConsumerIO,
      paymentReturnedConsumerIO)

    listOfIos.parSequence_
  }
}
