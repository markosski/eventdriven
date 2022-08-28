package eventdriven.transactions.domain.usecase

import eventdriven.transactions.domain.event.Event
import eventdriven.transactions.domain.event.account.{AccountCreated, AccountCreditLimitUpdated, AccountEvent}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.infrastructure.store.AccountInfoStore
import wvlet.log.LogSupport

import scala.util.Try

object ProcessAccountChangeEvents extends LogSupport {

  def apply[T <: AccountEvent](event: Event[T])(accountStore: AccountInfoStore): Either[Throwable, Unit] = event.payload match {
    case a: AccountCreated => {
      Try(accountStore.save(AccountInfo(a.accountId, a.cardNumber, a.creditLimit, a.zipOrPostal, a.state))).toEither
    }
    case a: AccountCreditLimitUpdated => {
      accountStore.get(a.accountId) match {
        case Some(item) => {
          if (item.creditLimit == a.oldCreditLimit) {
            val updatedAccount = item.copy(creditLimit = a.newCreditLimit)
            info(s"Account info after update: $updatedAccount")
            Try(accountStore.save(updatedAccount)).toEither
          } else {
            Left(new Exception("Validation of current credit limit failed"))
          }
        }
        case None => Left(new Exception(s"Account ${a.accountId} not found"))
      }
    }
    case _ => Right(())
  }
}
