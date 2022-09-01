package eventdriven.transactions.domain.event

object transaction {
  sealed trait TransactionEvent {
    val accountId: Int
  }
  case class TransactionDecisioned(accountId: Int, cardNumber: Long, transactionId: String, amount: Int, decision: String, declineReason: String, ruleVersion: String, createdOn: Int) extends TransactionEvent
  case class TransactionPaymentApplied(accountId: Int, paymentId: String, amount: Int, createdOn: Int) extends TransactionEvent
  case class TransactionPaymentReturned(accountId: Int, paymentId: String, amount: Int, createdOn: Int) extends TransactionEvent
}
