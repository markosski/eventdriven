package eventdriven.payments.domain.entity

object transaction {
  case class TransactionAccountSummary(accountId: Int, balance: Int, pending: Int, available: Int)
}
