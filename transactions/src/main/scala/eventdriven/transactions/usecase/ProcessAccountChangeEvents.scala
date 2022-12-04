package eventdriven.transactions.usecase

import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.domain.events.{AccountCreatedEvent, AccountCreditLimitUpdatedEvent}
import eventdriven.transactions.domain.entity.account.AccountInfo
import eventdriven.transactions.usecase.store.AccountInfoStore
import wvlet.log.LogSupport

import scala.util.Try

object ProcessAccountChangeEvents extends LogSupport {

  def apply[T](event: EventEnvelope[T])(accountStore: AccountInfoStore): Either[Throwable, Unit] = event.payload match {
    case a: AccountCreatedEvent => {
      Try(accountStore.save(AccountInfo(a.accountId, a.cardNumber, a.creditLimit, a.zipOrPostal, a.state))).toEither
    }
    case a: AccountCreditLimitUpdatedEvent => {
      accountStore.get(a.accountId) match {
        case Some(item) => {
          val updatedAccount = item.copy(creditLimit = a.newCreditLimit)
          info(s"Account info after update: $updatedAccount")
          Try(accountStore.save(updatedAccount)).toEither
        }
        case None => Left(new Exception(s"Account ${a.accountId} not found"))
      }
    }
    case _ => Right(())
  }
}
