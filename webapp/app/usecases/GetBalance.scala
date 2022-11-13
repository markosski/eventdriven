package usecases

import domain.transaction.TransactionAccountSummary
import services.TransactionService

object GetBalance {
  def apply(accountId: Int)(implicit transactionService: TransactionService): Either[Throwable, TransactionAccountSummary] = {
    transactionService.getBalance(accountId)
  }
}
