package eventdriven.payments.infrastructure

import AppConfig._

case class AppConfig(
                      kafkaConfig: KafkaConfig,
                      webConfig: WebConfig,
                      transactionServiceConfig: TransactionServiceConfig
                    )

object AppConfig {
  case class KafkaConfig(host: String, port: Int)
  case class WebConfig(port: Int)
  case class TransactionServiceConfig(port: Int, hostString: String)
}
