package eventdriven.transactions.domain.clearing

import eventdriven.core.integration.service.transactions.TransactionToClear
import eventdriven.transactions.domain.events.{SettlementCode, TransactionDecisionedEvent}

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
