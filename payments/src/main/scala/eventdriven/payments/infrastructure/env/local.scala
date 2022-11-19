package eventdriven.payments.infrastructure.env

import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaProducerConfig
import eventdriven.core.infrastructure.messaging.kafka.KafkaEventProducer
import eventdriven.payments.infrastructure.AppConfig
import eventdriven.payments.infrastructure.store.PaymentStoreInMemory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object local {

  def getEnv(config: AppConfig) = {
    val kconfig = KafkaProducerConfig(
      config.kafkaConfig.host,
      config.kafkaConfig.port,
      "org.apache.kafka.common.serialization.StringSerializer",
      "org.apache.kafka.common.serialization.StringSerializer")
    val publisher = new KafkaEventProducer("payments", kconfig)

    val outbox = ListBuffer[EventEnvelope[_]]()
    val paymentStore = new PaymentStoreInMemory(mutable.ListBuffer())

    Environment(paymentStore, publisher)
  }
}
