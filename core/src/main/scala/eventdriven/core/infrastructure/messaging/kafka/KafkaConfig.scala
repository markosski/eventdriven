package eventdriven.core.infrastructure.messaging.kafka

object KafkaConfig {
  case class KafkaProducerConfig(host: String, port: Int, keySerializer: String, valueSerializer: String)
  case class KafkaConsumerConfig(host: String, port: Int, groupId: String, keyDeserializer: String, valueDeserializer: String)
}
