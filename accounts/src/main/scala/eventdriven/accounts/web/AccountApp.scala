package eventdriven.accounts.web

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import wvlet.log.LogSupport
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import eventdriven.accounts.infrastructure.AppConfig
import eventdriven.accounts.infrastructure.env.local
import eventdriven.accounts.usecase.{GetAccount, UpdateCreditLimit}
import eventdriven.core.infrastructure.service.ErrorResponse
import eventdriven.core.infrastructure.service.accounts.{GetAccountResponse, UpdateCreditLimitRequest}
import eventdriven.core.util.json.anyToJson
import org.http4s.{HttpRoutes}
import org.http4s.dsl.io._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object AccountApp extends IOApp.Simple with LogSupport {
  val config = ConfigSource.default.load[AppConfig] match {
    case Left(err) => throw new Exception(err.toString())
    case Right(config) => config
  }
  val environment = local.getEnv(config)
  implicit val accountStore = environment.accountStore
  implicit val outboxPoller = environment.outboxPoller

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "_health" => Ok(s"""{"response": "healthy"}""")
    case GET -> Root / "accounts" / accountId =>
      GetAccount(accountId) match {
        case Right(account) => Ok(
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
          ))
        case Left(err) => Ok(ErrorResponse.toJson(err.getMessage))
      }
    case req @ PUT -> Root / "accounts" / accountId / "creditLimit" => {
      val payload = req.as[String].map(x => UpdateCreditLimitRequest.fromJson(x))
      payload.map { x =>
        UpdateCreditLimit(accountId, x.newCreditLimit) match {
          case Right(_) => Ok()
          case Left(err) => Ok(ErrorResponse.toJson(err.getMessage))
        }
      }.flatten
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

    val poller = IO.interruptible {
      outboxPoller.run()
    }.flatMap(_ => IO(ExitCode.Success))

    val listOfIos = List(app, poller)

    listOfIos.parSequence_
  }
}
