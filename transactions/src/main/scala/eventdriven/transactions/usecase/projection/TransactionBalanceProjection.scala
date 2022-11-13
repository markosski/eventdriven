package eventdriven.transactions.usecase.projection

import eventdriven.core.domain.Projection
import eventdriven.core.infrastructure.messaging.events.{TransactionDecisionedEvent, TransactionEvent, TransactionPaymentAppliedEvent, TransactionPaymentReturnedEvent}
import eventdriven.transactions.domain.model.transaction.TransactionBalance
import wvlet.log.LogSupport

class TransactionBalanceProjection(events: List[TransactionEvent]) extends Projection[TransactionEvent, TransactionBalance] with LogSupport {
  def get: Option[TransactionBalance] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      val state = events
        .foldLeft(TransactionBalance(events.head.accountId, 0)) {
          (state, trx) => trx match {
            case TransactionDecisionedEvent(_, _, _, amt, "Approved", _, _, _) => state.copy(balance = state.balance + amt)
            case TransactionPaymentAppliedEvent(_, _, amount, _) => state.copy(balance = state.balance - amount)
            case TransactionPaymentReturnedEvent(_, _, amount, _) => state.copy(balance = state.balance + amount)
            case _ => state
          }
        }
      Some(state)
    }
  }
}
