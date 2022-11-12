package eventdriven.accounts.infrastructure.web

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import wvlet.log.LogSupport
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import eventdriven.accounts.infrastructure.env.local
import eventdriven.accounts.infrastructure.web.serde.{ErrorResponseSerde, UpdateCreditLimitSerde}
import eventdriven.accounts.usecase.{GetAccount, UpdateCreditLimit}
import eventdriven.core.util.json.anyToJson
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object AccountApp extends IOApp.Simple with LogSupport {
  val environment = local.getEnv
  implicit val accountStore = environment.accountStore
  implicit val outboxPoller = environment.outboxPoller

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "_health" => Ok(s"""{"response": "healthy"}""")
    case GET -> Root / "accounts" / accountId =>
      GetAccount(accountId) match {
        case Right(response) => Ok(anyToJson(response))
        case Left(err) => Ok(ErrorResponseSerde.toJson(err.getMessage))
      }
    case req @ PUT -> Root / "accounts" / accountId / "updateCreditLimit" => {
      val payload = req.as[String].map(x => UpdateCreditLimitSerde.fromJson(x))
      payload.map { x =>
        UpdateCreditLimit(accountId, x.newCreditLimit) match {
          case Right(response) => Ok(anyToJson(response))
          case Left(err) => Ok(ErrorResponseSerde.toJson(err.getMessage))
        }
      }.flatten
    }
  }.orNotFound

  def run: IO[Unit] = {
    val app = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(routes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

    val poller = IO.interruptible {
      outboxPoller.run()
    }.flatMap(_ => IO(ExitCode.Success))

    val listOfIos = List(app, poller)

    listOfIos.parSequence_
  }
}
