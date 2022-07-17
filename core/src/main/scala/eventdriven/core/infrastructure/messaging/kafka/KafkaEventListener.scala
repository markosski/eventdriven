package eventdriven.core.infrastructure.messaging.kafka

import eventdriven.core.infrastructure.messaging.EventListener
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{KafkaConsumerConfig, KafkaProducerConfig}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer

import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters._

class KafkaEventListener(topic: String, clientId: String, config: KafkaConsumerConfig) extends EventListener[List[String]] {
  val host = s"${config.host}:${config.port}"
  val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host)
  props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId)
  props.put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId)
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.keyDeserializer)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.valueDeserializer)
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
  props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1)

  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(List(topic).asJava)

  override def take: Option[List[String]] = {
    val record = consumer.poll(Duration.ofMillis(250))
    val response = Some(record.iterator().asScala.toList.map(_.value()))
    consumer.commitSync()
    response
  }
}
