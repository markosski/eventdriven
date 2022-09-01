package eventdriven.transactions.domain.event
import eventdriven.transactions.util.json.mapper

import scala.util.Try

object account {
  trait AccountEvent {
    val accountId: Int
  }

  case class AccountCreated(accountId: Int, cardNumber: Long, creditLimit: Int, recordedTimestamp: Long, zipOrPostal: String, state: String) extends AccountEvent
  case class AccountCreditLimitUpdated(accountId: Int, oldCreditLimit: Int, newCreditLimit: Int, recordedTimestamp: Long) extends AccountEvent

  object AccountCreated {
    def fromJson(json: String): Either[Throwable, Event[AccountCreated]] = {
      Try(mapper.readValue[Event[AccountCreated]](json)).toEither
    }
  }

  object AccountCreditLimitUpdated {
    def fromJson(json: String): Either[Throwable, Event[AccountCreditLimitUpdated]] = {
      Try(mapper.readValue[Event[AccountCreditLimitUpdated]](json)).toEither
    }
  }
}
