package services

import eventdriven.core.integration.service.transactions.{AuthorizationDecisionResponse, GetAccountBalanceResponse, GetTransactionsResponse}

trait TransactionService {
  def getRecentTransactions(accountId: Int): Either[Throwable, GetTransactionsResponse]
  def getBalance(accountId: Int): Either[Throwable, GetAccountBalanceResponse]
  def makePurchase(cardNumber: Long, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: String): Either[Throwable, AuthorizationDecisionResponse]
}
