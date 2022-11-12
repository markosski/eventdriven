package eventdriven.accounts.infrastructure.store

import com.sun.org.slf4j.internal.LoggerFactory
import eventdriven.accounts.domain.account.{Account, AccountCreditLimitUpdatedEvent}
import eventdriven.accounts.usecase.AccountStore
import eventdriven.core.infrastructure.messaging.{EventEnvelopeMap, Topics}
import eventdriven.core.util.{json, string, time}

import scala.collection.mutable
import scala.util.Try

class AccountStoreInMemory(var data: mutable.ListBuffer[Account], var outbox: mutable.ListBuffer[EventEnvelopeMap]) extends AccountStore {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def get(accountId: Int): Either[Throwable, Account] = {
    data.find(_.accountId == accountId).toRight(new Exception("record not found"))
  }

  def getOutboxEvents(): Either[Throwable, List[EventEnvelopeMap]] = {
    Try(outbox.sortBy(_.eventTimeInMillis).reverse.toList).toEither
  }

  def deleteOutboxEvent(event: EventEnvelopeMap): Either[Throwable, Unit] = Try {
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
    val serialized = Map(
      "accountId" -> entity.accountId,
      "newCreditLimit" -> entity.creditLimit,
      "recordedTimestamp" -> time.unixTimestampNow()
    )
    val envelope = EventEnvelopeMap(string.getUUID(), Topics.AccountCreditLimitUpdatedV1.toString, time.unixTimestampNow(), serialized)
    outbox.append(envelope)
    logger.warn(s"outbox updated ${outbox.size}")
    ()
  }.toEither
}
