package eventdriven.transactions.web.serde

import eventdriven.core.util.json
import eventdriven.transactions.domain.entity.transaction.AuthorizationRequest

import scala.util.Try

object PreDecisionedTransactionRequestSerde {
  def fromJson(jsonString: String): Either[Throwable, AuthorizationRequest] = {
    Try(json.mapper.readValue[AuthorizationRequest](jsonString)).toEither
  }
}
