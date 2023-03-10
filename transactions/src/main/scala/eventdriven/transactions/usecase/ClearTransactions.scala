package eventdriven.transactions.usecase

import eventdriven.transactions.domain.events.TransactionEvent
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.core.integration.service.transactions.TransactionToClear
import eventdriven.transactions.domain.TransactionAggregate
import eventdriven.transactions.domain.entity.transaction.TransactionToClearResult

object ClearTransactions {
  def apply(transactionsToClear: List[TransactionToClear])(
           es: EventStore[TransactionEvent]
  ): List[Either[Throwable, TransactionToClearResult]] = {
    transactionsToClear.map { t =>
      for {
        allEvents <- es.get(t.accountId)
        aggregate = new TransactionAggregate(allEvents)
        payload <- aggregate.clearTransactions(t)
        _ <- es.append(payload)
      } yield TransactionToClearResult(t.accountId, t.transactionId, t.amount, payload.code.toString)
    }
  }
}
