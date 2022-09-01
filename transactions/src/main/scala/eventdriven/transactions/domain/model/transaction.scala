package eventdriven.transactions.domain.model

import eventdriven.transactions.util.json.mapper

import scala.util.Try

object transaction {
  case class TransactionSummary(accountId: Int, balance: Int)

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
