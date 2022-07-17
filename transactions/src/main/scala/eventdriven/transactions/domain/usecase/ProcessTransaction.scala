package eventdriven.transactions.domain.usecase

import eventdriven.core.infrastructure.messaging.EventDispatcher
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.aggregate.TransactionAggregate
import eventdriven.transactions.domain.event.{Event, PreDecisionedAuth}
import eventdriven.transactions.domain.event.transaction.TransactionEvent
import eventdriven.transactions.infrastructure.messaging.Topic
import eventdriven.transactions.infrastructure.store.{AccountInfoStore, PaymentSummaryStore}

object ProcessTransaction {
  def apply(preAuth: PreDecisionedAuth)(
    es: EventStore[TransactionEvent],
    acctInfoStore: AccountInfoStore,
    payments: PaymentSummaryStore,
    dispatcher: EventDispatcher[String]): Either[Throwable, Unit] = {
    for {
      aggregate <- TransactionAggregate.init(preAuth.account)(es, acctInfoStore, payments)
      payload <- aggregate.handle(preAuth)
      event = Event(payload, java.time.Instant.now().getEpochSecond)
      _ <- es.append(payload)
    } yield dispatcher.publish(payload.accountId.toString, event.toString, Topic.TransactionDecisioned.toString)
  }
}
