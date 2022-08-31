package eventdriven.core.infrastructure.messaging.kafka

import eventdriven.core.infrastructure.messaging.EventDispatcher
import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{KafkaConsumerConfig, KafkaProducerConfig}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import java.util.Properties
import scala.util.Try

class KafkaEventProducer(clientId: String, config: KafkaProducerConfig) extends EventDispatcher[String] {
  val host = s"${config.host}:${config.port}"
  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host)
  props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.keySerializer)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.valueSerializer)

  val producer = new KafkaProducer[String, String](props)

  override def publish(key: String, event: String, topic: String): Either[Throwable, Unit] = {
    Try(producer.send(new ProducerRecord[String, String](topic, key, event)).get()).toEither
      .fold(err => Left(err), _ => Right(()))
  }
}
