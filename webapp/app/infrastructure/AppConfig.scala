package infrastructure

import infrastructure.AppConfig.{
  AccountServiceConfig,
  TransactionServiceConfig,
  WebAppConfig,
  KafkaConfig
}

object AppConfig {
  case class WebAppConfig(port: Int)
  case class AccountServiceConfig(port: Int, hostString: String)
  case class TransactionServiceConfig(port: Int, hostString: String)
  case class KafkaConfig(host: String, port: Int)
}

case class AppConfig(
    webAppConfig: WebAppConfig,
    accountServiceConfig: AccountServiceConfig,
    transactionServiceConfig: TransactionServiceConfig,
    kafkaConfig: KafkaConfig
)
