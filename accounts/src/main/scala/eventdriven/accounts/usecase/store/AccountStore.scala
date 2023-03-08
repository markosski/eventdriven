package eventdriven.accounts.usecase.store

import eventdriven.accounts.domain.account.Account
import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.integration.events.AccountCreditLimitUpdatedEvent

trait AccountStore {
  def get(accountId: Int): Either[Throwable, Account]

  def getOutboxEvents(): Either[Throwable, List[EventEnvelope[AccountCreditLimitUpdatedEvent]]]

  def deleteOutboxEvent(event: EventEnvelope[AccountCreditLimitUpdatedEvent]): Either[Throwable, Unit]

  def saveOrUpdate(entity: Account): Either[Throwable, Unit]
}
