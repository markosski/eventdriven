package eventdriven.transactions.domain

import eventdriven.transactions.domain.entity.decision.Decision

object events {
  object SettlementCode extends Enumeration {
    val CLEAN, BAD = Value
  }

  sealed trait TransactionEvent {
    val accountId: Int
    val createdOn: Long
  }

  case class TransactionClearingResultEvent(accountId: Int, transactionId: String, amount: Int, code: SettlementCode.Value, createdOn: Long) extends TransactionEvent

  case class TransactionDecisionedEvent(accountId: Int, cardNumber: Long, transactionId: String, amount: Int, decision: Decision.Value, declineReason: String, ruleVersion: String, createdOn: Long) extends TransactionEvent

  case class TransactionPaymentAppliedEvent(accountId: Int, paymentId: String, amount: Int, createdOn: Long) extends TransactionEvent

  case class TransactionPaymentReturnedEvent(accountId: Int, paymentId: String, amount: Int, createdOn: Long) extends TransactionEvent

}
