package services

import domain.transaction.{DecisionedTransactionResponse, TransactionInfo}

trait TransactionService {
  def getRecentTransactions(accountId: Int): Either[Throwable, List[TransactionInfo]]
  def getBalance(accountId: Int): Either[Throwable, Int]
  def makePurchase(cardNumber: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: String): Either[Throwable, DecisionedTransactionResponse]
}
