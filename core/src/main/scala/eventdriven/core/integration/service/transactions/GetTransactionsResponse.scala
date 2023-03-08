package eventdriven.core.integration.service.transactions

object GetTransactionsResponse {
  trait TransactionInfoType {}
  case class TransactionInfoPurchase(accountId: Int, transactionId: String, amount: Int, decision: String, decisionReason: String, createdOn: Long) extends TransactionInfoType
  case class TransactionInfoPayment(accountId: Int, transactionId: String, amount: Int, createdOn: Long) extends TransactionInfoType
  case class TransactionInfo(category: String, transaction: TransactionInfoType)
  case class TransactionInfoResponse(transactions: List[TransactionInfo])
}

case class GetTransactionsResponse(transactions: List[GetTransactionsResponse.TransactionInfoType])
