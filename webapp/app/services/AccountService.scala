package services

import domain.account.Account

trait AccountService {
  def accountDetails(accountId: Int): Either[Throwable, Account]
  def updateCreditLimit(accountId: Int, newCreditLimit: Int): Either[Throwable, Unit]
}
