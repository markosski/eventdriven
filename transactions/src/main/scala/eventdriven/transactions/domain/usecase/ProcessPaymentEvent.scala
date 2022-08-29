package eventdriven.transactions.domain.usecase

import eventdriven.core.infrastructure.messaging.EventDispatcher
import eventdriven.core.infrastructure.store.{EventStore}
import eventdriven.transactions.domain.aggregate.TransactionSummaryAggregate
import eventdriven.transactions.domain.event.Event
import eventdriven.transactions.domain.event.payment.{PaymentEvent, PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.domain.event.transaction.TransactionEvent
import eventdriven.transactions.infrastructure.messaging.Topic

import java.util.UUID

/**
 * We want to deduplicate payment events and ensure we are not applying same event twice between transaction snapshots
 */
object ProcessPaymentEvent {
  def apply[T <: PaymentEvent](event: Event[T])(es: EventStore[TransactionEvent], dispatcher: EventDispatcher[String]): Either[Throwable, Unit] = event.payload match {
    case e: PaymentReturned => for {
      aggregate <- TransactionSummaryAggregate.init(e.accountId)(es)
      payload <- aggregate.handle(e)
      uuid = UUID.randomUUID().toString
      publishTopic = Topic.TransactionPaymentReturned.toString
      event = Event(payload, uuid, java.time.Instant.now().getEpochSecond)
      _ <- es.append(payload)
    } yield dispatcher.publish(payload.accountId.toString, event.toString, publishTopic)

    case e: PaymentSubmitted => for {
      aggregate <- TransactionSummaryAggregate.init(e.accountId)(es)
      payload <- aggregate.handle(e)
      uuid = UUID.randomUUID().toString
      publishTopic = Topic.TransactionPaymentApplied.toString
      event = Event(payload, uuid, java.time.Instant.now().getEpochSecond)
      _ <- es.append(payload)
    } yield dispatcher.publish(payload.accountId.toString, event.toString, publishTopic)
  }
}
