package eventdriven.transactions.domain.model

object decision {
  object Decision extends Enumeration {
    val Approved, Declined = Value
  }

  case class DecisionResult(decision: Decision.Value, ruleVersion: String, declineReason: Option[String])
}
