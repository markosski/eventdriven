package eventdriven.core.infrastructure.serde

import eventdriven.core.util.json.mapper

case class ErrorResponse(error: String)
object ErrorResponse {
  def toJson(errorMessage: String): String = {
    mapper.writeValueAsString(ErrorResponse(errorMessage))
  }
}
