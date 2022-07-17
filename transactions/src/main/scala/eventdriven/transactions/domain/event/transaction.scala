package eventdriven.transactions.domain.event

object transaction {
  sealed trait TransactionEvent {
    val accountId: Int
  }
  case class TransactionDecisioned(accountId: Int, transactionId: String, amount: Int, decision: String, declineReason: String, createdOn: Int) extends TransactionEvent
}
