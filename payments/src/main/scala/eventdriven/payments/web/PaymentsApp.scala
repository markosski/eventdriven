package eventdriven.payments.web

import wvlet.log.LogSupport
import eventdriven.payments.infrastructure.AppConfig
import eventdriven.payments.infrastructure.env.local
import eventdriven.payments.usecases.SubmitPayment
import eventdriven.payments.usecases.SubmitPayment.SubmitPaymentInput
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import eventdriven.core.infrastructure.messaging.Topics
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaConsumerConfig
import eventdriven.core.infrastructure.messaging.kafka.{
  KafkaConfig,
  KafkaEventListener
}
import eventdriven.core.integration.events.SubmitPaymentEvent

object PaymentsApp extends LogSupport {
  def main(args: Array[String]): Unit = {
    val config: AppConfig = ConfigSource.default.load[AppConfig] match {
      case Left(err)     => throw new Exception(err.toString())
      case Right(config) => config
    }
    val environment = local.getEnv(config)

    implicit val dispatcher = environment.eventPublisher
    implicit val paymentStore = environment.paymentStore
    implicit val transactionService = environment.transactionService

    implicit val system = ActorSystem(Behaviors.empty, "payments")
    implicit val executionContext = system.executionContext

    val kconfigConsumer = KafkaConsumerConfig(
      config.kafkaConfig.host,
      config.kafkaConfig.port,
      "group1",
      KafkaConfig.DESERIALIZER,
      KafkaConfig.DESERIALIZER
    )

    val submitPaymentConsumer = new KafkaEventListener(
      Topics.SubmitPaymentV1.toString,
      "payments",
      kconfigConsumer
    )

    val submitPaymentRunnable = new Runnable with LogSupport {
      def run = {
        info(s"Listening to ${Topics.SubmitPaymentV1}")
        while (true) {
          submitPaymentConsumer.take match {
            case Some(xs) =>
              xs.foreach { json =>
                for {
                  event <- SubmitPaymentEvent.fromJson(json)
                  _ = info(
                    s"Processing event ${Topics.SubmitPaymentV1}, payload: $event"
                  )
                  result <- SubmitPayment(
                    SubmitPaymentInput(
                      event.payload.accountId.toString,
                      event.payload.amount,
                      event.payload.source
                    )
                  )
                  _ = info(result)
                } yield ()
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

    executionContext.execute(submitPaymentRunnable)

    Http()
      .newServerAt("localhost", config.webConfig.port)
      .bind(concat(getHealth))
  }
}
