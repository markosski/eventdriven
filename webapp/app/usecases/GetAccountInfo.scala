package usecases

import domain.account.Account
import services.AccountService

object GetAccountInfo {
  def apply(accountId: Int)(implicit accountService: AccountService): Either[Throwable, Account] = {
    accountService.accountDetails(accountId)
  }
}
