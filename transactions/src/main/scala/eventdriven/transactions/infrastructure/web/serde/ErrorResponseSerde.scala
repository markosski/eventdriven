package eventdriven.transactions.infrastructure.web.serde

import eventdriven.transactions.util.json.mapper

case class ErrorResponseSerde(error: String)

object ErrorResponseSerde {
  def toJson(errorMessage: String): String = {
    mapper.writeValueAsString(ErrorResponseSerde(errorMessage))
  }
}
