package eventdriven.accounts.infrastructure.web

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import wvlet.log.LogSupport
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import eventdriven.accounts.domain.account.{Account, AccountCreditLimitUpdatedEvent, Address}
import eventdriven.accounts.infrastructure.outbox.AccountOutboxEventStore
import eventdriven.accounts.infrastructure.store.AccountStoreInMemory
import eventdriven.accounts.infrastructure.web.serde.{ErrorResponseSerde, UpdateCreditLimitSerde}
import eventdriven.accounts.usecase.{GetAccount, UpdateCreditLimit}
import eventdriven.core.infrastructure.messaging.EventEnvelopeMap
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaProducerConfig
import eventdriven.core.infrastructure.messaging.kafka.{KafkaEventListener, KafkaEventProducer, KafkaEventProducerGeneric}
import eventdriven.core.outboxpoller.impl.{OutboxPollerImpl, OutboxPublisherImpl}
import eventdriven.core.util.json
import eventdriven.core.util.json.anyToJson
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.collection.mutable.ListBuffer

object AccountApp extends IOApp.Simple with LogSupport {
  val outbox = ListBuffer[EventEnvelopeMap]()
  val data = ListBuffer[Account](
    Account(
      123,
      "12345678",
      50000,
      "John Doe", Address("13 Elm Street", "80126", "US"), "123456789"))

  implicit val accountStore = new AccountStoreInMemory(data, outbox)

  val kconfig = KafkaProducerConfig(
    "localhost",
    19092,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  implicit val dispatcher = new KafkaEventProducerGeneric[EventEnvelopeMap]("accounts", kconfig) {
    override def serialize(event: EventEnvelopeMap): String = {
      json.anyToJson(event)
    }
  }

  val outboxStore = new AccountOutboxEventStore(accountStore)
  val outboxPublisher = new OutboxPublisherImpl(dispatcher)
  implicit val outboxPoller = new OutboxPollerImpl(outboxStore, outboxPublisher)

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
