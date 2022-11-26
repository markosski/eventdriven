package eventdriven.transactions.domain.projection

import eventdriven.core.domain.Projection
import eventdriven.core.domain.events.{TransactionDecisionedEvent, TransactionEvent, TransactionPaymentAppliedEvent, TransactionPaymentReturnedEvent, TransactionSettlementResultEvent}
import eventdriven.transactions.domain.model.transaction.TransactionBalance
import wvlet.log.LogSupport

class TransactionBalanceProjection(events: List[TransactionEvent]) extends Projection[TransactionEvent, TransactionBalance] with LogSupport {
  def get: Option[TransactionBalance] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      val state = events
        .foldLeft(TransactionBalance(events.head.accountId, 0,0)) {
          (state, trx) => trx match {
            case TransactionDecisionedEvent(_, _, _, amount, "Approved", _, _, _) => state.copy(pending = state.pending + amount)
            case TransactionPaymentAppliedEvent(_, _, amount, _) => state.copy(balance = state.balance - amount)
            case TransactionPaymentReturnedEvent(_, _, amount, _) => state.copy(balance = state.balance + amount)
            case TransactionSettlementResultEvent(_, _, amount, "SETTLED", _) => state.copy(balance = state.balance + amount, pending = state.pending - amount)
            case TransactionSettlementResultEvent(_, _, amount, "VOIDED", _) => state.copy(pending = state.pending - amount)
            case _ => state
          }
        }
      Some(state)
    }
  }
}
