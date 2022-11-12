package services

import domain.account.Account

trait AccountService {
  def accountDetails(accountId: Int): Either[Throwable, Account]
}
