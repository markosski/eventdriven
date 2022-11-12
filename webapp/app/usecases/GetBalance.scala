package usecases

import services.TransactionService

object GetBalance {
  def apply(accountId: Int)(implicit transactionService: TransactionService): Either[Throwable, Int] = {
    transactionService.getBalance(accountId)
  }
}
