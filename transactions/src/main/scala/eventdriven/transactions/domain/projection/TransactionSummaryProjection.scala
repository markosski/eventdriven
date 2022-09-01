package eventdriven.transactions.domain.projection

import eventdriven.core.domain.Projection
import eventdriven.transactions.domain.event.transaction.{TransactionDecisioned, TransactionEvent, TransactionPaymentApplied, TransactionPaymentReturned}
import eventdriven.transactions.domain.model.transaction.TransactionSummary
import wvlet.log.LogSupport

class TransactionSummaryProjection(events: List[TransactionEvent]) extends Projection[TransactionEvent, TransactionSummary] with LogSupport {
  def get: Option[TransactionSummary] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      val state = events
        .foldLeft(TransactionSummary(events.head.accountId, 0)) {
          (state, trx) => trx match {
            case TransactionDecisioned(_, _, _, amt, "Approved", _, _, _) => state.copy(balance = state.balance + amt)
            case TransactionPaymentApplied(_, _, amount, _) => state.copy(balance = state.balance - amount)
            case TransactionPaymentReturned(_, _, amount, _) => state.copy(balance = state.balance + amount)
            case _ => state
          }
        }
      Some(state)
    }
  }
}
