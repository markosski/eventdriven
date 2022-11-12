package eventdriven.accounts.usecase

import eventdriven.accounts.domain.account.Account
import eventdriven.accounts.usecase.store.AccountStore

import scala.util.Try

object GetAccount {
  def apply(accountId: String)(implicit accountStore: AccountStore): Either[Throwable, Account] = for {
    accountIdValid <- validate(accountId)
    entity <- accountStore.get(accountIdValid)
  } yield entity


  def validate(accountId: String): Either[Throwable, Int] = {
    Try(accountId.toInt).toEither
  }
}
