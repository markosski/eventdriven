package eventdriven.transactions.domain

import eventdriven.core.models.Projection
import eventdriven.transactions.domain.entity.decision.Decision
import eventdriven.transactions.domain.entity.transaction.TransactionBalance
import eventdriven.transactions.domain.events._
import wvlet.log.LogSupport

class TransactionBalanceProjection(events: List[TransactionEvent]) extends Projection[TransactionEvent, TransactionBalance] with LogSupport {
  def get: Option[TransactionBalance] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      val state = events
        .foldLeft(TransactionBalance(events.head.accountId, 0,0)) {
          (state, trx) => trx match {
            case TransactionDecisionedEvent(_, _, _, amount, Decision.Approved, _, _, _) => state.copy(pending = state.pending + amount)
            case TransactionPaymentAppliedEvent(_, _, amount, _) => state.copy(balance = state.balance - amount)
            case TransactionPaymentReturnedEvent(_, _, amount, _) => state.copy(balance = state.balance + amount)
            case TransactionClearingResultEvent(_, _, amount, SettlementCode.CLEAN, _) =>
              state.copy(balance = state.balance + amount, pending = state.pending - amount)
            case _ => state
          }
        }
      Some(state)
    }
  }
}
