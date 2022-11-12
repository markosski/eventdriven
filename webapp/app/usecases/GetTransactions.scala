package usecases

import domain.transaction.{TransactionInfo, TransactionInfoPayment, TransactionInfoPurchase}
import services.TransactionService

object GetTransactions {
  def apply(accountId: Int)(implicit transactionService: TransactionService): Either[Throwable, List[TransactionInfo]] = {
    transactionService.getRecentTransactions(accountId)
  }
}
