package eventdriven.transactions.usecase

import eventdriven.transactions.domain.events.TransactionEvent
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.core.integration.events.TransactionDecisionedEvent
import eventdriven.core.integration.service.transactions.{AuthorizationDecisionRequest, AuthorizationDecisionResponse}
import eventdriven.core.util.{string, time}
import eventdriven.transactions.domain.TransactionAggregate
import eventdriven.transactions.usecase.store.AccountInfoStore

object AuthorizeTransaction {
  def apply(preAuth: AuthorizationDecisionRequest)(
    es: EventStore[TransactionEvent],
    acctInfoStore: AccountInfoStore,
    dispatcher: EventPublisher[String]): Either[Throwable, AuthorizationDecisionResponse] = {
    for {
      acctInfo <- acctInfoStore.getByCardNumber(preAuth.cardNumber).toRight(new Exception(s"could not find account for card number: ${preAuth.cardNumber}"))
      events <- es.get(acctInfo.accountId)
      aggregate = new TransactionAggregate(events)
      domainEvent <- aggregate.processAuthorization(preAuth, acctInfo)
      _ <- es.append(domainEvent)
      integrationEvent = TransactionDecisionedEvent(
        accountId = domainEvent.accountId,
        cardNumber = domainEvent.cardNumber,
        transactionId = domainEvent.transactionId,
        amount = domainEvent.amount,
        decision = domainEvent.decision.toString,
        declineReason = domainEvent.declineReason,
        ruleVersion = domainEvent.ruleVersion,
        createdOn = domainEvent.createdOn
      )
      eventMessage = EventEnvelope(string.getUUID(), Topics.TransactionDecisionedV1.toString, time.unixTimestampNow(), integrationEvent)
      _ <- dispatcher.publish(integrationEvent.accountId.toString, eventMessage.toString, Topics.TransactionDecisionedV1.toString)
    } yield AuthorizationDecisionResponse(preAuth.cardNumber, preAuth.transactionId, preAuth.amount, domainEvent.decision.toString)
  }
}
