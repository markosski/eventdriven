package eventdriven.transactions.domain.model

import eventdriven.core.util.json

import scala.util.Try

object transaction {
  case class TransactionBalance(accountId: Int, balance: Int)
  case class TransactionSummary(accountId: Int, balance: Int, available: Int)

  trait TransactionInfoType {}
  case class TransactionInfoPurchase(accountId: Int, transactionId: String, amount: Int, decision: String, decisionReason: String, createdOn: Int) extends TransactionInfoType
  case class TransactionInfoPayment(accountId: Int, transactionId: String, amount: Int, createdOn: Int) extends TransactionInfoType
  case class TransactionInfo(category: String, transaction: TransactionInfoType)
  case class TransactionInfoResponse(transactions: List[TransactionInfo])
  object TransactionInfoResponse {
    def toJson(resp: TransactionInfoResponse): String = {
      json.mapper.writeValueAsString(resp)
    }
  }

  case class PreDecisionedTransactionRequest(cardNumber: Int, transactionId: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: Int)
  object PreDecisionedTransactionRequest {
    def fromJson(jsonString: String): Either[Throwable, PreDecisionedTransactionRequest] = {
      Try(json.mapper.readValue[PreDecisionedTransactionRequest](jsonString)).toEither
    }
  }

  case class DecisionedTransactionResponse(cardNumber: Int, transactionId: String, amount: Int, decision: String)
  object DecisionedTransactionResponse {
    def toJson(resp: DecisionedTransactionResponse): String = {
      json.mapper.writeValueAsString(resp)
    }
  }
}
