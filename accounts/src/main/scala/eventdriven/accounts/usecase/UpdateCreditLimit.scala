package eventdriven.accounts.usecase

import eventdriven.accounts.domain.account.Account
import eventdriven.accounts.usecase.store.AccountStore
import eventdriven.core.outboxpoller.OutboxPoller

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
    _              = poller.poke()
  } yield newEntity

  def validate(accountId: String): Either[Throwable, Int] = {
    Try(accountId.toInt).toEither
  }
}
