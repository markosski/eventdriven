package eventdriven.transactions.infrastructure.web.serde

import eventdriven.transactions.util.json.mapper

case class OkResponse(success: String)

object OkResponseSerde {
  def toJson(message: String): String = {
    mapper.writeValueAsString(OkResponse(message))
  }
}
