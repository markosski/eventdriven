package eventdriven.accounts.infrastructure.store

import eventdriven.accounts.domain.account.Account
import eventdriven.accounts.usecase.store.AccountStore
import eventdriven.core.infrastructure.messaging.events.AccountCreditLimitUpdatedEvent
import eventdriven.core.infrastructure.messaging.{EventEnvelope, Topics}
import eventdriven.core.util.{string, time}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Try

class AccountStoreInMemory(var data: mutable.ListBuffer[Account], var outbox: mutable.ListBuffer[EventEnvelope[AccountCreditLimitUpdatedEvent]]) extends AccountStore {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def get(accountId: Int): Either[Throwable, Account] = {
    data.find(_.accountId == accountId).toRight(new Exception("record not found"))
  }

  def getOutboxEvents(): Either[Throwable, List[EventEnvelope[AccountCreditLimitUpdatedEvent]]] = {
    Try(outbox.sortBy(_.eventTimeInMillis).reverse.toList).toEither
  }

  def deleteOutboxEvent(event: EventEnvelope[AccountCreditLimitUpdatedEvent]): Either[Throwable, Unit] = Try {
    val idx = outbox.indexOf(event)
    outbox.remove(idx)
    ()
  }.toEither

  def saveOrUpdate(entity: Account): Either[Throwable, Unit] = {
    data = data.filter(_.accountId != entity.accountId)
    data.append(entity)
    saveToOutbox(entity)
    Right(())
  }

  private def saveToOutbox(entity: Account): Either[Throwable, Unit] = Try {
    val payload = AccountCreditLimitUpdatedEvent(entity.accountId, entity.creditLimit, time.unixTimestampNow())
    val envelope = EventEnvelope(string.getUUID(), Topics.AccountCreditLimitUpdatedV1.toString, time.unixTimestampNow(), payload)
    outbox.append(envelope)
    logger.info(s"outbox updated ${outbox.size}")
  }.toEither
}
