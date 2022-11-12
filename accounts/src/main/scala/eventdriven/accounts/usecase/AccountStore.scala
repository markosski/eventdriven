package eventdriven.accounts.usecase

import eventdriven.accounts.domain.account.{Account, AccountCreditLimitUpdatedEvent}
import eventdriven.core.infrastructure.messaging.EventEnvelopeMap

trait AccountStore {
  def get(accountId: Int): Either[Throwable, Account]

  def getOutboxEvents(): Either[Throwable, List[EventEnvelopeMap]]

  def deleteOutboxEvent(event: EventEnvelopeMap): Either[Throwable, Unit]

  def saveOrUpdate(entity: Account): Either[Throwable, Unit]
}
