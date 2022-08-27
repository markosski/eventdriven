package eventdriven.transactions.domain.model

object account {
  case class AccountInfo(accountId: Int, cardNumber: Long, creditLimit: Int, zipOrPostal: String, state: String)
  case class AccountTransactionSummary(accountId: Int, cardNumber: Long, creditLimit: Int, balance: Int, zipOrPostal: String, state: String)
}
