package eventdriven.transactions.usecase

import eventdriven.core.integration.events.{PaymentEvent, PaymentReturnedEvent, PaymentSubmittedEvent}
import eventdriven.transactions.domain.events.TransactionEvent
import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.TransactionAggregate

/**
 * We want to deduplicate payment events and ensure we are not applying same event twice between transaction snapshots
 */
object ProcessPaymentEvent {
  def apply[T <: PaymentEvent](event: EventEnvelope[T])(es: EventStore[TransactionEvent]): Either[Throwable, Unit] =
    event.payload match {
    case e: PaymentReturnedEvent => for {
      events <- es.get(e.accountId)
      aggregate = new TransactionAggregate(events)
      payload <- aggregate.applyReturnedPayment(e)
      _ <- es.append(payload)
    } yield ()

    case e: PaymentSubmittedEvent => for {
      events <- es.get(e.accountId)
      aggregate = new TransactionAggregate(events)
      payload <- aggregate.applyPayment(e)
      _ <- es.append(payload)
    } yield ()
  }
}
