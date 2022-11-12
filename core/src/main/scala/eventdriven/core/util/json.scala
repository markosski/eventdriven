package eventdriven.core.util

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}

object json {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build() :: ClassTagExtensions

  def anyToJson(obj: Any) = {
    mapper.writeValueAsString(obj)
  }
}
