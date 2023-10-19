package eventdriven.accounts.web

import wvlet.log.LogSupport
import eventdriven.accounts.infrastructure.AppConfig
import eventdriven.accounts.infrastructure.env.local
import eventdriven.accounts.usecase.{GetAccount, UpdateCreditLimit}
import eventdriven.core.integration.service.ErrorResponse
import eventdriven.core.integration.service.accounts.{
  GetAccountResponse,
  UpdateCreditLimitRequest
}
import eventdriven.core.util.json.anyToJson
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import eventdriven.core.infrastructure.messaging.Topics
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{
  KafkaConsumerConfig,
  KafkaProducerConfig
}
import eventdriven.core.infrastructure.messaging.kafka.{
  KafkaConfig,
  KafkaEventListener,
  KafkaEventProducer
}
import eventdriven.core.integration.events.UpdateCreditLimitEvent

import scala.io.StdIn

object AccountApp extends LogSupport {
  def main(args: Array[String]): Unit = {
    val config: AppConfig = ConfigSource.default.load[AppConfig] match {
      case Left(err)     => throw new Exception(err.toString())
      case Right(config) => config
    }
    val environment = local.getEnv(config)

    val kconfigConsumer = KafkaConsumerConfig(
      config.kafkaConfig.host,
      config.kafkaConfig.port,
      "group1",
      KafkaConfig.DESERIALIZER,
      KafkaConfig.DESERIALIZER
    )

    val updateCreditLimitConsumer = new KafkaEventListener(
      Topics.UpdateCreditLimitV1.toString,
      "accounts",
      kconfigConsumer
    )

    implicit val accountStore = environment.accountStore
    implicit val outboxPoller = environment.outboxPoller

    implicit val system = ActorSystem(Behaviors.empty, "accounts")
    implicit val executionContext = system.executionContext

    val updateCreditLimitRunnable = new Runnable with LogSupport {
      def run = {
        info(s"Listening to $Topics.UpdateCreditLimitV1")
        while (true) {
          updateCreditLimitConsumer.take match {
            case Some(xs) =>
              xs.foreach { json =>
                for {
                  event <- UpdateCreditLimitEvent.fromJson(json)
                  _ = info(
                    s"Processing event ${Topics.UpdateCreditLimitV1}, payload: $event"
                  )
                  result <- UpdateCreditLimit(
                    event.payload.accountId.toString,
                    event.payload.newCreditLimit
                  )
                  _ = info(result)
                } yield ()
              }
            case None => ()
          }
        }
      }
    }

    executionContext.execute(updateCreditLimitRunnable)
    executionContext.execute(() => outboxPoller.run())

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

    val getAccounts =
      path("accounts" / """\d+""".r) { accountId =>
        get {
          val resp = GetAccount(accountId) match {
            case Right(account) =>
              anyToJson(
                GetAccountResponse(
                  account.accountId,
                  account.cardNumber,
                  account.creditLimit,
                  account.fullName,
                  GetAccountResponse.Address(
                    account.address.streetAddress,
                    account.address.zipOrPostal,
                    account.address.countryCode
                  ),
                  account.phoneNumber
                )
              )
            case Left(err) => ErrorResponse.toJson(err.getMessage)
          }
          complete(HttpEntity(ContentTypes.`application/json`, resp))
        }
      }

    Http()
      .newServerAt("localhost", config.webConfig.port)
      .bind(concat(getHealth, getAccounts))
  }
}
