package eventdriven.transactions.domain.event

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}

import scala.util.Try

object payment {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build() :: ClassTagExtensions

  case class PaymentSubmitted(accountId: Int, paymentId: String, amount: Int)
  case class PaymentReturned(accountId: Int, paymentId: String, amount: Int, reason: String)

  object PaymentSubmitted {
    def fromJson(json: String): Either[Throwable, Event[PaymentSubmitted]] = {
      Try(mapper.readValue[Event[PaymentSubmitted]](json)).toEither
    }
  }

  object PaymentReturned {
    def fromJson(json: String): Either[Throwable, Event[PaymentReturned]] = {
      Try(mapper.readValue[Event[PaymentReturned]](json)).toEither
    }
  }
}
