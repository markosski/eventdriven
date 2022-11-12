package eventdriven.accounts.usecase

import eventdriven.accounts.domain.account.{Account, AccountCreditLimitUpdatedEvent}
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.outboxpoller.OutboxPoller
import eventdriven.core.util.{json, string, time}

import scala.util.Try

object UpdateCreditLimit {
  def apply(accountId: String, newCL: Int)
           (implicit
            accountStore: AccountStore,
            poller: OutboxPoller): Either[Throwable, Account] = for
  {
    accountIdValid <- validate(accountId)
    entity         <- accountStore.get(accountIdValid)
    newEntity      = entity.copy(creditLimit = newCL)
    _              <- accountStore.saveOrUpdate(newEntity)
//    payload        = AccountCreditLimitUpdatedEvent(accountIdValid, entity.creditLimit, newEntity.creditLimit, time.unixTimestampNow())
//    envelope       = EventEnvelope(string.getUUID(), Topics.AccountCreditLimitUpdatedV1.toString, "1", time.unixTimestampNow(), payload)
    _              = poller.poke()
//    _              <- dispatcher.publish(payload.accountId.toString, json.anyToJson(envelope), Topics.AccountCreditLimitUpdatedV1.toString)
  } yield newEntity

  def validate(accountId: String): Either[Throwable, Int] = {
    Try(accountId.toInt).toEither
  }
}
