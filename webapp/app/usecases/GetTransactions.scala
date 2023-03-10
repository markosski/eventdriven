package usecases

import eventdriven.core.integration.service.transactions.GetTransactionsResponse
import services.TransactionService

object GetTransactions {
  def apply(accountId: Int)(implicit transactionService: TransactionService): Either[Throwable, GetTransactionsResponse] = {
    transactionService.getRecentTransactions(accountId)
  }
}
