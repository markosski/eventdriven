package eventdriven.transactions.usecase

import eventdriven.core.domain.events.{TransactionEvent}
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.core.util.{string, time}
import eventdriven.transactions.domain.aggregate.TransactionAggregate
import eventdriven.transactions.domain.entity.transaction.{TransactionClearingResponse, TransactionToClear, TransactionToClearResult}

object ClearTransactions {
  def apply(transactionsToClear: List[TransactionToClear])(
           es: EventStore[TransactionEvent],
           dispatcher: EventPublisher[String]
  ): List[Either[Throwable, TransactionToClearResult]] = {
    transactionsToClear.map { t =>
      for {
        allEvents <- es.get(t.accountId)
        aggregate = new TransactionAggregate(allEvents)
        payload <- aggregate.handle(t)
        event = EventEnvelope(string.getUUID(), Topics.TransactionClearingResponseV1.toString, time.unixTimestampNow(), payload)
        _ <- es.append(payload)
        _ <- dispatcher.publish(payload.accountId.toString, event.toString, Topics.TransactionClearingResponseV1.toString)
      } yield TransactionToClearResult(t.accountId, t.transactionId, t.amount, payload.code.toString)
    }
  }
}
