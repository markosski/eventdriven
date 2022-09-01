package eventdriven.transactions.domain.event

import eventdriven.transactions.util.json.mapper

import scala.util.Try

object payment {
  sealed trait PaymentEvent {
    val accountId: Int
    val paymentId: String
  }
  case class PaymentSubmitted(accountId: Int, paymentId: String, amount: Int, recordedTimestamp: Long) extends PaymentEvent
  case class PaymentReturned(accountId: Int, paymentId: String, amount: Int, reason: String, recordedTimestamp: Long) extends PaymentEvent

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
