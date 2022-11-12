package eventdriven.payments.infrastructure.web.serde

import eventdriven.core.util.json.mapper

case class ErrorResponseSerde(error: String)

object ErrorResponseSerde {
  def toJson(errorMessage: String): String = {
    mapper.writeValueAsString(ErrorResponseSerde(errorMessage))
  }
}