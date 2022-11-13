package eventdriven.accounts.infrastructure.store

import eventdriven.accounts.usecase.store.AccountStore
import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.domain.events.AccountCreditLimitUpdatedEvent
import eventdriven.core.outboxpoller.OutboxEventStore

class AccountOutboxEventStore(store: AccountStore) extends OutboxEventStore[EventEnvelope[AccountCreditLimitUpdatedEvent]] {
  def getUnpublished(limit: Int): Either[Throwable, List[EventEnvelope[AccountCreditLimitUpdatedEvent]]] = {
    store.getOutboxEvents()
  }

  def deleteEvent(event: EventEnvelope[AccountCreditLimitUpdatedEvent]): Either[Throwable, Unit] = {
    store.deleteOutboxEvent(event)
  }
}
