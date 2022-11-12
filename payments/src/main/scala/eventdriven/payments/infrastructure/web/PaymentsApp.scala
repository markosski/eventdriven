package eventdriven.payments.infrastructure.web

import org.http4s.ember.server.EmberServerBuilder
import wvlet.log.LogSupport
import com.comcast.ip4s._
import cats.effect.{ExitCode, IO, IOApp}
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaProducerConfig
import eventdriven.core.infrastructure.messaging.kafka.{KafkaEventProducer}
import eventdriven.payments.infrastructure.store.PaymentStoreInMemory
import eventdriven.payments.infrastructure.web.serde.{ErrorResponseSerde, SubmitPaymentSerde}
import eventdriven.payments.usecases.SubmitPayment
import eventdriven.payments.usecases.SubmitPayment.SubmitPaymentInput
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.collection.mutable

object PaymentsApp extends IOApp.Simple with LogSupport {
  val kconfig = KafkaProducerConfig(
    "localhost",
    19092,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  implicit val dispatcher = new KafkaEventProducer("payments", kconfig)
  implicit val paymentStore = new PaymentStoreInMemory(mutable.ListBuffer())

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "_health" => Ok(s"""{"response": "healthy"}""")
    case req @ POST -> Root / "payments" / accountId => {
      val payload = req.as[String].map(x => SubmitPaymentSerde.fromJson(x))
      payload.map { x =>
        val input = SubmitPaymentInput(accountId, x.amount, x.source)
        SubmitPayment(input) match {
          case Right(response) => Ok(s"""{"response": "$response"}""")
          case Left(err) => Ok(ErrorResponseSerde.toJson(err.getMessage))
        }
      }.flatten
    }
  }.orNotFound

  def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8082")
      .withHttpApp(routes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
