package eventdriven.payments.infrastructure.web

import cats.effect._
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server._

object WebApp extends IOApp {
  val helloWorldService = HttpRoutes.of[IO] {
    case POST -> Root / "payments" / "submit" => Ok(s"Hello.")
    case GET -> Root / "payments" / accountId / timeAsOf => Ok("")
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(helloWorldService)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
