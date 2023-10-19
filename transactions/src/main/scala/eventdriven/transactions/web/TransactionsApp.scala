package eventdriven.transactions.web

import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{
  KafkaConsumerConfig,
  KafkaProducerConfig
}
import eventdriven.core.infrastructure.messaging.kafka.{
  KafkaConfig,
  KafkaEventListener,
  KafkaEventProducer
}
import wvlet.log.LogSupport
import eventdriven.core.infrastructure.messaging.Topics
import eventdriven.core.integration.events.{
  AccountCreditLimitUpdatedEvent,
  PaymentReturnedEvent,
  PaymentSubmittedEvent
}
import eventdriven.core.integration.service.ErrorResponse
import eventdriven.core.integration.service.transactions.{
  AuthorizationDecisionRequest,
  ClearTransactionsRequest,
  ClearTransactionsResponse
}
import eventdriven.core.util.json
import eventdriven.transactions.infrastructure.AppConfig
import eventdriven.transactions.infrastructure.env.local
import eventdriven.transactions.usecase.{
  AuthorizeTransaction,
  ClearTransactions,
  GetAccountSummary,
  GetRecentTransactions,
  ProcessAccountChangeEvents,
  ProcessPaymentEvent
}
import pureconfig._
import pureconfig.generic.auto._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

object TransactionsApp extends LogSupport {
  def main(args: Array[String]): Unit = {
    val config: AppConfig = ConfigSource.default.load[AppConfig] match {
      case Left(err)     => throw new Exception(err.toString())
      case Right(config) => config
    }
    val environment = local.getEnv

    val kconfig = KafkaProducerConfig(
      config.kafkaConfig.host,
      config.kafkaConfig.port,
      KafkaConfig.SERIALIZER,
      KafkaConfig.SERIALIZER
    )
    val dispatcher = new KafkaEventProducer("transactions", kconfig)

    val kconfigConsumer = KafkaConsumerConfig(
      config.kafkaConfig.host,
      config.kafkaConfig.port,
      "group1",
      KafkaConfig.DESERIALIZER,
      KafkaConfig.DESERIALIZER
    )

    val accountCreditLimitUpdated = new KafkaEventListener(
      Topics.AccountCreditLimitUpdatedV1.toString,
      "transactions",
      kconfigConsumer
    )
    val paymentSubmittedConsumer = new KafkaEventListener(
      Topics.PaymentSubmittedV1.toString,
      "transactions",
      kconfigConsumer
    )
    val paymentReturnedConsumer = new KafkaEventListener(
      Topics.PaymentReturnedV1.toString,
      "transactions",
      kconfigConsumer
    )

    implicit val system = ActorSystem(Behaviors.empty, "transactions")
    implicit val executionContext = system.executionContext

    val accountCreditLimitUpdatedConsumerRunnable = new Runnable
      with LogSupport {
      def run = {
        info("Listening to accountCreditLimitUpdated")
        while (true) {
          accountCreditLimitUpdated.take match {
            case Some(xs) =>
              xs.foreach { json =>
                (for {
                  accountEvent <- AccountCreditLimitUpdatedEvent.fromJson(json)
                  _ = info(
                    s"Processing event accountCreditLimitUpdated, payload: $accountEvent"
                  )
                  _ <- ProcessAccountChangeEvents(accountEvent)(
                    environment.accountInfoStore
                  )
                } yield ()) match {
                  case Left(err) => error(err.getMessage)
                  case Right(_) =>
                    info(
                      s"Event from ${Topics.AccountCreditLimitUpdatedV1.toString} processed successfully"
                    )
                }
              }
            case None => ()
          }
        }
      }
    }

    val paymentSubmittedConsumerRunnable = new Runnable with LogSupport {
      def run = {
        info("Listening to paymentSubmitted")
        while (true) {
          paymentSubmittedConsumer.take match {
            case Some(xs) =>
              xs.foreach { json =>
                (for {
                  payment <- PaymentSubmittedEvent.fromJson(json)
                  _ = info(
                    s"Processing event paymentSubmitted, payload: $payment"
                  )
                  result <- ProcessPaymentEvent(payment)(
                    environment.transactionStore
                  )
                  _ = info(result)
                } yield ()) match {
                  case Left(err) => error(err.getMessage)
                  case Right(_) =>
                    info(
                      s"Event from ${Topics.PaymentSubmittedV1.toString} processed successfully"
                    )
                }
              }
            case None => ()
          }
        }
      }
    }

    val paymentReturnedConsumerRunnable = new Runnable with LogSupport {
      def run = {
        info("Listening to paymentReturned")
        while (true) {
          paymentReturnedConsumer.take match {
            case Some(xs) =>
              xs.foreach { json =>
                (for {
                  payment <- PaymentReturnedEvent.fromJson(json)
                  _ = info(
                    s"Processing event paymentReturned, payload: $payment"
                  )
                  result <- ProcessPaymentEvent(payment)(
                    environment.transactionStore
                  )
                  _ = info(result)
                } yield ()) match {
                  case Left(err) => error(err.getMessage)
                  case Right(_) =>
                    info(
                      s"Event from ${Topics.PaymentReturnedV1.toString} processed successfully"
                    )
                }
              }
            case None => ()
          }
        }
      }
    }

    val getHealth =
      path("_health") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              """{"response": "healthy"}"""
            )
          )
        }
      }

    val authorize =
      path("authorize") {
        post {
          entity(as[String]) { raw =>
            val resp = (for {
              preAuth <- AuthorizationDecisionRequest.fromJson(raw)
              _ = info(s"Received process transaction request: $preAuth")
              processed <- AuthorizeTransaction(preAuth)(
                environment.transactionStore,
                environment.accountInfoStore,
                dispatcher
              )
            } yield processed) match {
              case Left(err) => {
                err.printStackTrace()
                ErrorResponse.toJson(err.getMessage)
              }
              case Right(resp) => {
                info(s"Process transaction response: $resp")
                json.anyToJson(resp)
              }
            }
            complete(HttpEntity(ContentTypes.`application/json`, resp))
          }
        }
      }

    val getBalance =
      path("balance" / """\d+""".r) { accountIdString =>
        {
          get {
            info(
              s"Received account summary request for account $accountIdString"
            )
            val accountId = Integer.parseInt(accountIdString)
            val resp = GetAccountSummary(accountId)(
              environment.transactionStore,
              environment.accountInfoStore
            ) match {
              case Right(resp) => {
                info(s"Account summary transaction response: $resp")
                json.anyToJson(resp)
              }
              case Left(err) => {
                err.printStackTrace()
                ErrorResponse.toJson(err.getMessage)
              }
            }
            complete(HttpEntity(ContentTypes.`application/json`, resp))
          }
        }
      }

    val getTransactions =
      path("transactions" / """\d+""".r) { accountIdString =>
        {
          get {
            info(
              s"Received recent transactions request for account $accountIdString"
            )
            val accountId = Integer.parseInt(accountIdString)
            val resp = GetRecentTransactions(accountId)(
              environment.transactionStore
            ) match {
              case Right(resp) => {
                info(s"Account recent transactions response: $resp")
                json.anyToJson(resp)
              }
              case Left(err) => {
                err.printStackTrace()
                ErrorResponse.toJson(err.getMessage)
              }
            }
            complete(HttpEntity(ContentTypes.`application/json`, resp))
          }
        }
      }

    val clearAuths =
      path("clearAuths") {
        post {
          entity(as[String]) { body =>
            val resp = (for {
              input <- ClearTransactionsRequest.fromJson(body)
              result = ClearTransactions(input)(environment.transactionStore)
            } yield result) match {
              case Left(err) => {
                err.printStackTrace()
                ErrorResponse.toJson(err.getMessage)
              }
              case Right(resp) => {
                info(s"Process transaction response: $resp")
                val response = ClearTransactionsResponse(
                  resp.map {
                    case Left(err) =>
                      ClearTransactionsResponse.ClearingResult(
                        None,
                        Some(err.getMessage)
                      )
                    case Right(result) =>
                      ClearTransactionsResponse.ClearingResult(
                        Some(
                          ClearTransactionsResponse.TransactionClearingResult(
                            result.accountId,
                            result.transactionId,
                            result.amount,
                            result.code
                          )
                        ),
                        error = None
                      )
                  }
                )
                json.anyToJson(response)
              }
            }
            complete(HttpEntity(ContentTypes.`application/json`, resp))
          }
        }
      }

    executionContext.execute(accountCreditLimitUpdatedConsumerRunnable)
    executionContext.execute(paymentSubmittedConsumerRunnable)
    executionContext.execute(paymentReturnedConsumerRunnable)

    Http()
      .newServerAt("localhost", config.webConfig.port)
      .bind(
        concat(getHealth, authorize, getBalance, getTransactions, clearAuths)
      )
  }
}
