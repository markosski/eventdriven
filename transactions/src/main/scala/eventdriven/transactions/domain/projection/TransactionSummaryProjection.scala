package eventdriven.transactions.domain.projection

import eventdriven.core.domain.Projection
import eventdriven.transactions.domain.event.transaction.{TransactionDecisioned, TransactionEvent}
import eventdriven.transactions.domain.model.transaction.TransactionSummary

class TransactionSummaryProjection(events: List[TransactionEvent]) extends Projection[TransactionEvent, TransactionSummary] {
  def get: Option[TransactionSummary] = {
    if (events.isEmpty) None
    else {
      val state = events
        .foldLeft(TransactionSummary(events.head.accountId, 0)) {
          (state, trx) => trx match {
            case TransactionDecisioned(_, _, _, amt, "Approved", _, _, _) => state.copy(balance = state.balance + amt)
            case _ => state
          }
        }
      Some(state)
    }
  }
}
