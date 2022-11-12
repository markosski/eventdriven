package eventdriven.transactions.domain.event
import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.transactions.util.json.mapper

import scala.util.Try

object account {
  trait AccountEvent {
    val accountId: Int
  }

  case class AccountCreated(accountId: Int, cardNumber: Long, creditLimit: Int, recordedTimestamp: Long, zipOrPostal: String, state: String) extends AccountEvent
  case class AccountCreditLimitUpdated(accountId: Int, newCreditLimit: Int, recordedTimestamp: Long) extends AccountEvent

  object AccountCreated {
    def fromJson(json: String): Either[Throwable, EventEnvelope[AccountCreated]] = {
      Try(mapper.readValue[EventEnvelope[AccountCreated]](json)).toEither
    }
  }

  object AccountCreditLimitUpdated {
    def fromJson(json: String): Either[Throwable, EventEnvelope[AccountCreditLimitUpdated]] = {
      Try(mapper.readValue[EventEnvelope[AccountCreditLimitUpdated]](json)).toEither
    }
  }
}
