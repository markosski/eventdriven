package eventdriven.transactions.usecase

import eventdriven.transactions.domain.events.{TransactionDecisionedEvent, TransactionEvent, TransactionPaymentAppliedEvent}
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.entity.transaction.{TransactionInfo, TransactionInfoPayment, TransactionInfoPurchase, TransactionInfoResponse}

object GetRecentTransactions {
  def apply(accountId: Int)(es: EventStore[TransactionEvent]): Either[Throwable, TransactionInfoResponse] = {
    val transactions = for {
      events <- es.get(accountId)
      eventsSorted = events.reverse
    } yield eventsSorted.collect {
      case TransactionDecisionedEvent(accountId, _, transactionId, amount, decision, reason, _, createdOn) =>
        TransactionInfo(
          "purchase",
          TransactionInfoPurchase(accountId, transactionId, amount, decision.toString, reason, createdOn)
        )
      case TransactionPaymentAppliedEvent(accountId, paymentId, amount, createdOn) =>
        TransactionInfo(
          "payment",
          TransactionInfoPayment(accountId, paymentId, amount, createdOn)
        )
    }.take(30)

    transactions.map(x => TransactionInfoResponse(x))
  }
}
