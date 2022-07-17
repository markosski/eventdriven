package eventdriven.transactions.infrastructure.web.serde

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class OkResponse(success: String)

object OkResponseSerde {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  def toJson(message: String): String = {
    mapper.writeValueAsString(OkResponse(message))
  }
}
