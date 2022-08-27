package eventdriven.transactions.infrastructure.web.serde

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import eventdriven.transactions.domain.event.Event
import eventdriven.transactions.domain.event.transaction.PreDecisionedTransactionRequest

import scala.util.Try

object ProcessTransactionSerde {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build() :: ClassTagExtensions

  def fromJson(json: String): Either[Throwable, Event[PreDecisionedTransactionRequest]] = {
    println(json)
    Try(mapper.readValue[Event[PreDecisionedTransactionRequest]](json)).toEither
  }
}
