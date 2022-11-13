package eventdriven.transactions.infrastructure.web.serde

import eventdriven.core.util.json

case class ErrorResponseSerde(error: String)

object ErrorResponseSerde {
  def toJson(errorMessage: String): String = {
    json.mapper.writeValueAsString(ErrorResponseSerde(errorMessage))
  }
}
