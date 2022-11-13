package domain

object transaction {
  trait TransactionInfoType {
    val accountId: Int
    val transactionId: String
    val amount: Int
  }
  case class TransactionAccountSummary(accountId: Int, balance: Int, available: Int)
  case class TransactionInfoPurchase(accountId: Int, transactionId: String, amount: Int, decision: String, decisionReason: String, createdOn: Int) extends TransactionInfoType
  case class TransactionInfoPayment(accountId: Int, transactionId: String, amount: Int, createdOn: Int) extends TransactionInfoType
  case class TransactionInfo(category: String, transaction: TransactionInfoType)
  case class DecisionedTransactionResponse(cardNumber: Int, transactionId: String, amount: Int, decision: String)
}
