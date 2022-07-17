package eventdriven.core

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

object App {
  def main(args: Array[String]): Unit = {
    println("start")
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "test")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

    val producer = new KafkaProducer[String, String](props)
    val record = new ProducerRecord[String, String]("quickstart-events", "1", "some event")
    producer.send(record).get()
    producer.flush()
    producer.close()
    println("end")
  }
}
