package eventdriven.transactions.domain.model

import eventdriven.transactions.util.json.mapper

object account {
  case class AccountInfo(accountId: Int, cardNumber: Long, creditLimit: Int, zipOrPostal: String, state: String)

  case class AccountTransactionSummary(accountId: Int, cardNumber: Long, creditLimit: Int, balance: Int, zipOrPostal: String, state: String)

  case class AccountSummaryResponse(accountId: Int, creditLimit: Int, balance: Int, zipOrPostal: String, state: String)
  object AccountSummaryResponse {
    def toJson(accountSummary: AccountTransactionSummary): String = {
      mapper.writeValueAsString(accountSummary)
    }
  }
}
