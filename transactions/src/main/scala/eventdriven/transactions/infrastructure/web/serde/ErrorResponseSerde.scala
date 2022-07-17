package eventdriven.transactions.infrastructure.web.serde

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class ErrorResponseSerde(error: String)

object ErrorResponseSerde {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  def toJson(errorMessage: String): String = {
    mapper.writeValueAsString(ErrorResponseSerde(errorMessage))
  }
}
