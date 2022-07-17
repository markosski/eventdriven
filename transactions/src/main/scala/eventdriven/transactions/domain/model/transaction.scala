package eventdriven.transactions.domain.model

object transaction {
  object Decision extends Enumeration {
    val Approved, Declined = Value
  }

  case class TransactionSummary(accountId: Int, balance: Int)
  case class DecisionResult(decision: Decision.Value, declineReason: Option[String])
}
