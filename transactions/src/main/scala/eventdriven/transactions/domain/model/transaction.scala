package eventdriven.transactions.domain.model

import eventdriven.transactions.domain.model.decision.Decision
import eventdriven.transactions.util.json.mapper

import scala.util.Try

object transaction {
  case class TransactionSummary(accountId: Int, balance: Int)

  trait TransactionInfoType {}
  case class TransactionInfoPurchase(accountId: Int, transactionId: String, amount: Int, decision: String, decisionReason: String, createdOn: Int) extends TransactionInfoType
  case class TransactionInfoPayment(accountId: Int, transactionId: String, amount: Int, createdOn: Int) extends TransactionInfoType
  case class TransactionInfo(category: String, transaction: TransactionInfoType)
  case class TransactionInfoResponse(transactions: List[TransactionInfo])
  object TransactionInfoResponse {
    def toJson(resp: TransactionInfoResponse): String = {
      mapper.writeValueAsString(resp)
    }
  }

  case class PreDecisionedTransactionRequest(cardNumber: Int, transactionId: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: Int)
  object PreDecisionedTransactionRequest {
    def fromJson(json: String): Either[Throwable, PreDecisionedTransactionRequest] = {
      Try(mapper.readValue[PreDecisionedTransactionRequest](json)).toEither
    }
  }

  case class DecisionedTransactionResponse(cardNumber: Int, transactionId: String, amount: Int, decision: String)
  object DecisionedTransactionResponse {
    def toJson(resp: DecisionedTransactionResponse): String = {
      mapper.writeValueAsString(resp)
    }
  }
}
