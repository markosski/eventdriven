package eventdriven.transactions.domain.clearing

import eventdriven.core.domain.events.{SettlementCode, TransactionDecisionedEvent}
import eventdriven.core.infrastructure.service.transactions.TransactionToClear

object Clearing {
  def clearTransaction(authedTransaction: TransactionDecisionedEvent, toClear: TransactionToClear): SettlementCode.Value = {
    if (
        authedTransaction.transactionId == toClear.transactionId &&
        authedTransaction.amount == toClear.amount
    ) {
      SettlementCode.CLEAN
    } else {
      SettlementCode.BAD
    }
  }
}
