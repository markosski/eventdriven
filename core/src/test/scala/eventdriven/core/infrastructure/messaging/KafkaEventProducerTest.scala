package eventdriven.core.infrastructure.messaging

import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{KafkaConsumerConfig, KafkaProducerConfig}
import eventdriven.core.infrastructure.messaging.kafka.{KafkaConfig, KafkaEventListener, KafkaEventProducer}
import munit.FunSuite
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

class KafkaEventProducerTest extends FunSuite {
  test("test producer") {
    val config = KafkaProducerConfig(
      "localhost",
      19092,
      "org.apache.kafka.common.serialization.StringSerializer",
      "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaEventProducer("quickstart-events", (e: String) => "1", "test", config)
    val response = producer.publish("transaction 123")
    println(response)
  }

  test("test consumer") {
    val config = KafkaConsumerConfig(
      "localhost",
      19092,
      "group1",
      "org.apache.kafka.common.serialization.StringDeserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    val listener = new KafkaEventListener("quickstart-events", "test", config)
    for (i <- 0 to 10) {
      println(listener.take)
    }
  }
}
