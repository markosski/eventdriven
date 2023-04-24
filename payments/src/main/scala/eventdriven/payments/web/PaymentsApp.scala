package eventdriven.payments.web

import wvlet.log.LogSupport
import eventdriven.core.integration.service.ErrorResponse
import eventdriven.core.integration.service.payments.{SubmitPaymentRequest, SubmitPaymentResponse}
import eventdriven.core.util.json
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
import scala.io.StdIn

object PaymentsApp extends LogSupport {
  def main(args: Array[String]) {
    val config = ConfigSource.default.load[AppConfig] match {
      case Left(err) => throw new Exception(err.toString())
      case Right(config) => config
    }
    val environment = local.getEnv(config)

    implicit val dispatcher = environment.eventPublisher
    implicit val paymentStore = environment.paymentStore
    implicit val transactionService = environment.transactionService

    implicit val system = ActorSystem(Behaviors.empty, "payments")
    implicit val executionContext = system.executionContext

    val getHealth =
      path("_health") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, """{"response": "healthy"}"""))
        }
      }

    val postPayment =
      path("payments" / """\d+""".r) { 
        accountId =>
          post {
            entity(as[String]) { raw =>
              val payload = SubmitPaymentRequest.fromJson(raw)
              info(s"Processing payment with following payload: $payload")
              val input = SubmitPaymentInput(accountId, payload.amount, payload.source)
              val resp = SubmitPayment(input) match {
                case Right(paymentId) => json.anyToJson(SubmitPaymentResponse(paymentId))
                case Left(err) => ErrorResponse.toJson(err.getMessage)
              }
              complete(HttpEntity(ContentTypes.`application/json`, resp))
            }
          }
      }

    val bindingFuture = Http()
      .newServerAt(
        "localhost", 
        config.webConfig.port)
      .bind(concat(getHealth, postPayment))
  }
}
