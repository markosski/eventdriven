package usecases

import eventdriven.core.integration.service.transactions.GetAccountBalanceResponse
import services.TransactionService

object GetBalance {
  def apply(accountId: Int)(implicit transactionService: TransactionService): Either[Throwable, GetAccountBalanceResponse] = {
    transactionService.getBalance(accountId)
  }
}
