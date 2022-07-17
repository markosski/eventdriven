package eventdriven.transactions.domain.model

object account {
  case class AccountInfo(accountId: Int, creditLimit: Int, zipOrPostal: String, state: String)
  case class AccountSummary(accountId: Int, creditLimit: Int, balance: Int, zipOrPostal: String, state: String)
}
