package eventdriven.core.integration.service.transactions

object ClearTransactionsResponse {
  case class TransactionClearingResult(accountId: Int, transactionId: String, amount: Int, code: String)
  case class ClearingResult(result: Option[TransactionClearingResult], error: Option[String])
}
case class ClearTransactionsResponse(transactions: List[ClearTransactionsResponse.ClearingResult])

