package infrastructure.web

import AppConfig.{AccountServiceConfig, PaymentServiceConfig, TransactionServiceConfig, WebAppConfig}

object AppConfig {
  case class WebAppConfig(port: Int)
  case class AccountServiceConfig(port: Int, hostString: String)
  case class PaymentServiceConfig(port: Int, hostString: String)
  case class TransactionServiceConfig(port: Int, hostString: String)
}

case class AppConfig(
                    webAppConfig: WebAppConfig,
                    accountServiceConfig: AccountServiceConfig,
                    paymentServiceConfig: PaymentServiceConfig,
                    transactionServiceConfig: TransactionServiceConfig,
                    )
