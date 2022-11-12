package infrastructure.web

import AppConfig.{AccountServiceConfig, PaymentServiceConfig, TransactionServiceConfig, WebAppConfig}

object AppConfig {
  case class WebAppConfig(port: Int, host: String)
  case class AccountServiceConfig(port: Int, host: String)
  case class PaymentServiceConfig(port: Int, host: String)
  case class TransactionServiceConfig(port: Int, host: String)
}

case class AppConfig(
                    webAppConfig: WebAppConfig,
                    accountServiceConfig: AccountServiceConfig,
                    paymentServiceConfig: PaymentServiceConfig,
                    transactionServiceConfig: TransactionServiceConfig,
                    )
