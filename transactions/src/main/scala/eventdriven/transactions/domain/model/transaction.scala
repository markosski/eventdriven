package eventdriven.transactions.domain.model

object transaction {
  case class TransactionSummary(accountId: Int, balance: Int)
}
