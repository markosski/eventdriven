package eventdriven.transactions.usecase

import eventdriven.core.domain.events.{PaymentEvent, PaymentReturnedEvent, PaymentSubmittedEvent, TransactionEvent}
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.core.util.{string, time}
import eventdriven.transactions.domain.aggregate.TransactionAggregate

/**
 * We want to deduplicate payment events and ensure we are not applying same event twice between transaction snapshots
 */
object ProcessPaymentEvent {
  def apply[T <: PaymentEvent](event: EventEnvelope[T])(es: EventStore[TransactionEvent], dispatcher: EventPublisher[String]): Either[Throwable, Unit] =
    event.payload match {
    case e: PaymentReturnedEvent => for {
      events <- es.get(e.accountId)
      aggregate = new TransactionAggregate(events)
      payload <- aggregate.handle(e)
      publishTopic = Topics.TransactionPaymentReturnedV1.toString
      event = EventEnvelope(string.getUUID(), publishTopic, time.unixTimestampNow(), payload)
      _ <- es.append(payload)
    } yield dispatcher.publish(payload.accountId.toString, event.toString, publishTopic)

    case e: PaymentSubmittedEvent => for {
      events <- es.get(e.accountId)
      aggregate = new TransactionAggregate(events)
      payload <- aggregate.handle(e)
      publishTopic = Topics.TransactionPaymentAppliedV1.toString
      event = EventEnvelope(string.getUUID(), publishTopic, time.unixTimestampNow(), payload)
      _ <- es.append(payload)
    } yield dispatcher.publish(payload.accountId.toString, event.toString, publishTopic)
  }
}
