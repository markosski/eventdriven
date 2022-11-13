package eventdriven.transactions.usecase

import eventdriven.core.domain.events.TransactionEvent
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.core.util.{string, time}
import eventdriven.transactions.domain.aggregate.TransactionDecisionAggregate
import eventdriven.transactions.domain.model.transaction.{DecisionedTransactionResponse, PreDecisionedTransactionRequest}
import eventdriven.transactions.usecase.store.AccountInfoStore

object ProcessTransaction {
  def apply(preAuth: PreDecisionedTransactionRequest)(
    es: EventStore[TransactionEvent],
    acctInfoStore: AccountInfoStore,
    dispatcher: EventPublisher[String]): Either[Throwable, DecisionedTransactionResponse] = {
    for {
      acctInfo <- acctInfoStore.getByCardNumber(preAuth.cardNumber).toRight(new Exception(s"could not find account for card number: ${preAuth.cardNumber}"))
      events <- es.get(acctInfo.accountId)
      aggregate = new TransactionDecisionAggregate(events)
      payload <- aggregate.handle(preAuth, acctInfo)
      event = EventEnvelope(string.getUUID(), Topics.TransactionDecisionedV1.toString, time.unixTimestampNow(), payload)
      _ <- es.append(payload)
      _ <- dispatcher.publish(payload.accountId.toString, event.toString, Topics.TransactionDecisionedV1.toString)
    } yield DecisionedTransactionResponse(preAuth.cardNumber, preAuth.transactionId, preAuth.amount, payload.decision)
  }
}
