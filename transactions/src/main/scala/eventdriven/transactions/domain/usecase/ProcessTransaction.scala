package eventdriven.transactions.domain.usecase

import eventdriven.core.infrastructure.messaging.EventDispatcher
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.aggregate.TransactionSummaryAggregate
import eventdriven.transactions.domain.event.Event
import eventdriven.transactions.domain.event.transaction.{PreDecisionedTransactionRequest, TransactionEvent}
import eventdriven.transactions.infrastructure.messaging.Topic
import eventdriven.transactions.infrastructure.store.{AccountInfoStore}
import java.util.UUID

object ProcessTransaction {
  def apply(preAuth: PreDecisionedTransactionRequest)(
    es: EventStore[TransactionEvent],
    acctInfoStore: AccountInfoStore,
    dispatcher: EventDispatcher[String]): Either[Throwable, TransactionEvent] = {
    for {
      acctInfo <- acctInfoStore.getByCardNumber(preAuth.cardNumber).toRight(new Exception(s"could not find account for card number: ${preAuth.cardNumber}"))
      aggregate <- TransactionSummaryAggregate.init(acctInfo.accountId)(es)
      payload <- aggregate.handle(preAuth, acctInfo)
      uuid = UUID.randomUUID().toString
      event = Event(payload, uuid, Topic.TransactionDecisioned.toString, java.time.Instant.now().getEpochSecond)
      _ <- es.append(payload)
      _ <- dispatcher.publish(payload.accountId.toString, event.toString, Topic.TransactionDecisioned.toString)
    } yield event.payload
  }
}
