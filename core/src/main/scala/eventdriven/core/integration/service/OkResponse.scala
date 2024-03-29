package eventdriven.core.integration.service

import eventdriven.core.util.json

case class OkResponse(success: String)

object OkResponseSerde {
  def toJson(message: String): String = {
    json.mapper.writeValueAsString(OkResponse(message))
  }
}
