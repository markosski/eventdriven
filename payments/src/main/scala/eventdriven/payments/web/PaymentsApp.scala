package eventdriven.payments.web

import org.http4s.ember.server.EmberServerBuilder
import wvlet.log.LogSupport
import com.comcast.ip4s._
import cats.effect.{ExitCode, IO, IOApp}
import eventdriven.core.infrastructure.serde.ErrorResponse
import eventdriven.core.infrastructure.serde.payments.{SubmitPaymentRequest, SubmitPaymentResponse}
import eventdriven.core.util.json
import eventdriven.payments.infrastructure.AppConfig
import eventdriven.payments.infrastructure.env.local
import eventdriven.payments.usecases.SubmitPayment
import eventdriven.payments.usecases.SubmitPayment.SubmitPaymentInput
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object PaymentsApp extends IOApp.Simple with LogSupport {
  val config = ConfigSource.default.load[AppConfig] match {
    case Left(err) => throw new Exception(err.toString())
    case Right(config) => config
  }
  val environment = local.getEnv(config)
  implicit val dispatcher = environment.eventPublisher
  implicit val paymentStore = environment.paymentStore
  implicit val transactionService = environment.transactionService

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "_health" => Ok(s"""{"response": "healthy"}""")
    case req @ POST -> Root / "payments" / accountId => {
      val payload = req.as[String].map(x => SubmitPaymentRequest.fromJson(x))
      payload.map { x =>
        info(s"Processing payment with following payload: $x")
        val input = SubmitPaymentInput(accountId, x.amount, x.source)
        SubmitPayment(input) match {
          case Right(paymentId) => Ok(json.anyToJson(SubmitPaymentResponse(paymentId)))
          case Left(err) => Ok(ErrorResponse.toJson(err.getMessage))
        }
      }.flatten
    }
  }.orNotFound

  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(config.webConfig.port).getOrElse(port"0"))
      .withHttpApp(routes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
