package eventdriven.accounts.infrastructure.env

import eventdriven.accounts.domain.account.{Account, Address}
import eventdriven.accounts.infrastructure.store.{AccountOutboxEventStore, AccountStoreInMemory}
import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.domain.events.AccountCreditLimitUpdatedEvent
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaProducerConfig
import eventdriven.core.infrastructure.messaging.kafka.KafkaEventProducerGeneric
import eventdriven.core.outboxpoller.impl.{OutboxPollerBlockingImpl, OutboxPublisherImpl}
import eventdriven.core.util.json

import scala.collection.mutable.ListBuffer

object local {
  private val outbox = ListBuffer[EventEnvelope[AccountCreditLimitUpdatedEvent]]()
  private val data = ListBuffer[Account](
    Account(
      123,
      "12345678",
      50000,
      "John Doe", Address("13 Elm Street", "80126", "US"), "123456789"))

  private val kconfig = KafkaProducerConfig(
    "localhost",
    19092,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")

  private val accountStore = new AccountStoreInMemory(data, outbox)
  private val publisher = new KafkaEventProducerGeneric[EventEnvelope[AccountCreditLimitUpdatedEvent], String]("accounts", kconfig) {
    override def serialize(event: EventEnvelope[AccountCreditLimitUpdatedEvent]): String = {
      json.anyToJson(event)
    }
  }

  private val outboxStore = new AccountOutboxEventStore(accountStore)
  private val outboxPublisher = new OutboxPublisherImpl(publisher)
  private val outboxPoller = new OutboxPollerBlockingImpl(outboxStore, outboxPublisher)

  def getEnv = Environment(accountStore, outboxPoller)
}
