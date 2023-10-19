package services

import eventdriven.core.integration.service.accounts.GetAccountResponse

trait AccountService {
  def accountDetails(accountId: Int): Either[Throwable, GetAccountResponse]
}
