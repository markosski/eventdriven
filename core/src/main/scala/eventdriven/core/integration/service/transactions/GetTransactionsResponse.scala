package eventdriven.core.integration.service.transactions

object GetTransactionsResponse {
  trait TransactionInfoType {
    val accountId: Int
    val transactionId: String
    val amount: Int
  }
  case class TransactionInfoPurchase(accountId: Int, transactionId: String, amount: Int, decision: String, decisionReason: String, createdOn: Long) extends TransactionInfoType
  case class TransactionInfoPayment(accountId: Int, transactionId: String, amount: Int, createdOn: Long) extends TransactionInfoType
  case class TransactionInfo(category: String, transaction: TransactionInfoType)
}

case class GetTransactionsResponse(transactions: List[GetTransactionsResponse.TransactionInfo])
