package eventdriven.accounts.infrastructure.outbox

import eventdriven.accounts.usecase.AccountStore
import eventdriven.core.infrastructure.messaging.EventEnvelopeMap
import eventdriven.core.outboxpoller.OutboxEventStore

class AccountOutboxEventStore(store: AccountStore) extends OutboxEventStore[EventEnvelopeMap] {
  def getUnpublished(limit: Int): Either[Throwable, List[EventEnvelopeMap]] = {
    store.getOutboxEvents()
  }

  def deleteEvent(event: EventEnvelopeMap): Either[Throwable, Unit] = {
    store.deleteOutboxEvent(event)
  }
}
