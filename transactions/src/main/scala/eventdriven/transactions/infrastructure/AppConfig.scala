package eventdriven.transactions.infrastructure

import AppConfig._

case class AppConfig(
                      kafkaConfig: KafkaConfig,
                      webConfig: WebConfig
                    )

object AppConfig {
  case class KafkaConfig(host: String, port: Int)
  case class WebConfig(port: Int)
}
