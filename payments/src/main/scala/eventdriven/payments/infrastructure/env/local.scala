package eventdriven.payments.infrastructure.env

import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.KafkaProducerConfig
import eventdriven.core.infrastructure.messaging.kafka.KafkaEventProducer
import eventdriven.payments.infrastructure.store.PaymentStoreInMemory

import scala.collection.mutable

object local {
  private val kconfig = KafkaProducerConfig(
    "localhost",
    19092,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  private val publisher = new KafkaEventProducer("payments", kconfig)
  private val paymentStore = new PaymentStoreInMemory(mutable.ListBuffer())

  def getEnv = Environment(paymentStore, publisher)
}
