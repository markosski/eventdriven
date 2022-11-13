package eventdriven.transactions.usecase

import eventdriven.core.domain.events.{TransactionDecisionedEvent, TransactionEvent, TransactionPaymentAppliedEvent}
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.model.transaction.{TransactionInfo, TransactionInfoPayment, TransactionInfoPurchase, TransactionInfoResponse}

object GetRecentTransactions {
  def apply(accountId: Int)(es: EventStore[TransactionEvent]): Either[Throwable, TransactionInfoResponse] = {
    val transactions = for {
      events <- es.get(accountId)
      eventsSorted = events.reverse.take(30)
    } yield eventsSorted.map {
      case TransactionDecisionedEvent(accountId, cardNumber, transactionId, amount, decision, reason, _, createdOn) =>
        TransactionInfo(
          "purchase",
          TransactionInfoPurchase(accountId, transactionId, amount, decision, reason, createdOn)
        )
      case TransactionPaymentAppliedEvent(accountId, paymentId, amount, createdOn) =>
        TransactionInfo(
          "payment",
          TransactionInfoPayment(accountId, paymentId, amount, createdOn)
        )
    }

    transactions.map(x => TransactionInfoResponse(x))
  }
}
