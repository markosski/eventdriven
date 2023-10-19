package eventdriven.accounts.infrastructure.env

import eventdriven.accounts.domain.account.{Account, Address}
import eventdriven.accounts.infrastructure.AppConfig
import eventdriven.accounts.infrastructure.store.{
  AccountOutboxEventStore,
  AccountStoreInMemory
}
import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.integration.events.AccountCreditLimitUpdatedEvent
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaProducerConfig
import eventdriven.core.infrastructure.messaging.kafka.{
  KafkaConfig,
  KafkaEventProducerGeneric
}
import eventdriven.core.outboxpoller.impl.{
  OutboxPollerBlockingImpl,
  OutboxPublisherImpl
}
import eventdriven.core.util.json

import scala.collection.mutable.ListBuffer

object local {

  def getEnv(config: AppConfig) = {
    val outbox = ListBuffer[EventEnvelope[AccountCreditLimitUpdatedEvent]]()
    val data = ListBuffer[Account](
      Account(
        123,
        "12345678",
        50000,
        "John Doe",
        Address("13 Elm Street", "80126", "US"),
        "123456789"
      )
    )

    val kconfig = KafkaProducerConfig(
      config.kafkaConfig.host,
      config.kafkaConfig.port,
      KafkaConfig.SERIALIZER,
      KafkaConfig.SERIALIZER
    )

    val accountStore = new AccountStoreInMemory(data, outbox)
    val publisher = new KafkaEventProducerGeneric[EventEnvelope[
      AccountCreditLimitUpdatedEvent
    ], String]("accounts", kconfig) {
      override def serialize(
          event: EventEnvelope[AccountCreditLimitUpdatedEvent]
      ): String = {
        json.anyToJson(event)
      }
    }

    val outboxStore = new AccountOutboxEventStore(accountStore)
    val outboxPublisher = new OutboxPublisherImpl(publisher)
    val outboxPoller =
      new OutboxPollerBlockingImpl(outboxStore, outboxPublisher)

    Environment(accountStore, outboxPoller)
  }
}
