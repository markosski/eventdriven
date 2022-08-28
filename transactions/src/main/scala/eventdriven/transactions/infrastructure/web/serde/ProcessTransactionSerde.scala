package eventdriven.transactions.infrastructure.web.serde

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import eventdriven.transactions.domain.event.transaction.{PreDecisionedTransactionRequest, TransactionEvent}

import scala.util.Try

object ProcessTransactionSerde {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build() :: ClassTagExtensions

  def fromJson(json: String): Either[Throwable, PreDecisionedTransactionRequest] = {
    println(json)
    Try(mapper.readValue[PreDecisionedTransactionRequest](json)).toEither
  }

  def toJson(response: TransactionEvent): String = {
    println(response)
    mapper.writeValueAsString(response)
  }
}
