package eventdriven.transactions.domain.event

import eventdriven.transactions.util.json.mapper

object transaction {
  sealed trait TransactionEvent {
    val accountId: Int
  }

  case class TransactionDecisioned(accountId: Int, cardNumber: Long, transactionId: String, amount: Int, decision: String, declineReason: String, ruleVersion: String, createdOn: Int) extends TransactionEvent
  case class TransactionPaymentApplied(accountId: Int, paymentId: String, amount: Int, createdOn: Int) extends TransactionEvent
  case class TransactionPaymentReturned(accountId: Int, paymentId: String, amount: Int, createdOn: Int) extends TransactionEvent

  object TransactionDecisioned {
    def toJson(response: TransactionDecisioned): String = {
      mapper.writeValueAsString(response)
    }
  }

  object TransactionPaymentApplied {
    def toJson(event: TransactionPaymentApplied): String = {
      mapper.writeValueAsString(event)
    }
  }

  object TransactionPaymentReturned {
    def toJson(event: TransactionPaymentReturned): String = {
      mapper.writeValueAsString(event)
    }
  }
}
