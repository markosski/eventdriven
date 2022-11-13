package services

import domain.transaction.{DecisionedTransactionResponse, TransactionAccountSummary, TransactionInfo}

trait TransactionService {
  def getRecentTransactions(accountId: Int): Either[Throwable, List[TransactionInfo]]
  def getBalance(accountId: Int): Either[Throwable, TransactionAccountSummary]
  def makePurchase(cardNumber: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: String): Either[Throwable, DecisionedTransactionResponse]
}
