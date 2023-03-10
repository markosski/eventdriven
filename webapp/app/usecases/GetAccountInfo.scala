package usecases

import eventdriven.core.integration.service.accounts.GetAccountResponse
import services.AccountService

object GetAccountInfo {
  def apply(accountId: Int)(implicit accountService: AccountService): Either[Throwable, GetAccountResponse] = {
    accountService.accountDetails(accountId)
  }
}
