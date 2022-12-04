package eventdriven.core.domain

import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.util.json.mapper

import scala.util.Try

object events {
  case class AccountCreatedEvent(accountId: Int, cardNumber: Long, creditLimit: Int, recordedTimestamp: Long, zipOrPostal: String, state: String)

  case class AccountCreditLimitUpdatedEvent(accountId: Int, newCreditLimit: Int, recordedTimestamp: Long)

  sealed trait PaymentEvent {
    val accountId: Int
    val paymentId: String
  }

  case class PaymentReturnedEvent(accountId: Int, paymentId: String, amount: Int, reason: String, recordedTimestamp: Long) extends PaymentEvent

  case class PaymentSubmittedEvent(accountId: Int, paymentId: String, amount: Int, recordedTimestamp: Long) extends PaymentEvent

  sealed trait TransactionEvent {
    val accountId: Int
    val createdOn: Long
  }

  object SettlementCode extends Enumeration {
    val CLEAN, BAD = Value
  }

  case class TransactionClearingResultEvent(accountId: Int, transactionId: String, amount: Int, code: SettlementCode.Value, createdOn: Long) extends TransactionEvent

  case class TransactionDecisionedEvent(accountId: Int, cardNumber: Long, transactionId: String, amount: Int, decision: String, declineReason: String, ruleVersion: String, createdOn: Long) extends TransactionEvent

  case class TransactionPaymentAppliedEvent(accountId: Int, paymentId: String, amount: Int, createdOn: Long) extends TransactionEvent

  case class TransactionPaymentReturnedEvent(accountId: Int, paymentId: String, amount: Int, createdOn: Long) extends TransactionEvent

  object AccountCreatedEvent {
    def fromJson(json: String): Either[Throwable, EventEnvelope[AccountCreatedEvent]] = {
      Try(mapper.readValue[EventEnvelope[AccountCreatedEvent]](json)).toEither
    }
  }

  object AccountCreditLimitUpdatedEvent {
    def fromJson(json: String): Either[Throwable, EventEnvelope[AccountCreditLimitUpdatedEvent]] = {
      Try(mapper.readValue[EventEnvelope[AccountCreditLimitUpdatedEvent]](json)).toEither
    }
  }

  object PaymentSubmittedEvent {
    def fromJson(json: String): Either[Throwable, EventEnvelope[PaymentSubmittedEvent]] = {
      Try(mapper.readValue[EventEnvelope[PaymentSubmittedEvent]](json)).toEither
    }
  }

  object PaymentReturnedEvent {
    def fromJson(json: String): Either[Throwable, EventEnvelope[PaymentReturnedEvent]] = {
      Try(mapper.readValue[EventEnvelope[PaymentReturnedEvent]](json)).toEither
    }
  }
}
