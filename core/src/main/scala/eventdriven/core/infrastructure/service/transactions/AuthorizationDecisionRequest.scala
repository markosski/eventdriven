package eventdriven.core.infrastructure.service.transactions

import eventdriven.core.util.json

import scala.util.Try

case class AuthorizationDecisionRequest(cardNumber: Long, transactionId: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: Int)

object AuthorizationDecisionRequest {
  def fromJson(jsonString: String): Either[Throwable, AuthorizationDecisionRequest] = {
    Try(json.mapper.readValue[AuthorizationDecisionRequest](jsonString)).toEither
  }
}
