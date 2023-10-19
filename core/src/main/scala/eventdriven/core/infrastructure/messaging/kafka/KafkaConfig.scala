package eventdriven.core.infrastructure.messaging.kafka

object KafkaConfig {
  val SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer"
  val DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer"
  case class KafkaProducerConfig(
      host: String,
      port: Int,
      keySerializer: String,
      valueSerializer: String
  )
  case class KafkaConsumerConfig(
      host: String,
      port: Int,
      groupId: String,
      keyDeserializer: String,
      valueDeserializer: String
  )
}
