package eventdriven.transactions.domain.entity

object account {
  case class AccountInfo(accountId: Int, cardNumber: Long, creditLimit: Int, zipOrPostal: String, state: String)
}
