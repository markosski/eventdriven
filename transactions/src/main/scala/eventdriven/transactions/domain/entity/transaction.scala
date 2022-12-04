package eventdriven.transactions.domain.entity

object transaction {
  case class TransactionBalance(accountId: Int, balance: Int, pending: Int)
  case class TransactionSummary(accountId: Int, balance: Int, pending: Int, available: Int)

  trait TransactionInfoType {}
  case class TransactionInfoPurchase(accountId: Int, transactionId: String, amount: Int, decision: String, decisionReason: String, createdOn: Long) extends TransactionInfoType
  case class TransactionInfoPayment(accountId: Int, transactionId: String, amount: Int, createdOn: Long) extends TransactionInfoType
  case class TransactionInfo(category: String, transaction: TransactionInfoType)
  case class TransactionInfoResponse(transactions: List[TransactionInfo])

  case class TransactionToClear(accountId: Int, transactionId: String, amount: Int)
  case class TransactionClearingRequest(transactions: List[TransactionToClear])
  case class TransactionToClearResult(accountId: Int, transactionId: String, amount: Int, code: String)
  case class TransactionClearingResponse(transactions: List[Either[Throwable, TransactionToClearResult]])

  case class AuthorizationRequest(cardNumber: Int, transactionId: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: Int)
  case class AuthorizationDecisionedResponse(cardNumber: Int, transactionId: String, amount: Int, decision: String)
}
