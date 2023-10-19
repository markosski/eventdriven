package eventdriven.core.integration

import eventdriven.core.infrastructure.messaging.EventEnvelope
import eventdriven.core.util.json.mapper

import scala.util.Try

object events {
  case class AccountCreatedEvent(
      accountId: Int,
      cardNumber: Long,
      creditLimit: Int,
      recordedTimestamp: Long,
      zipOrPostal: String,
      state: String
  )

  case class AccountCreditLimitUpdatedEvent(
      accountId: Int,
      newCreditLimit: Int,
      recordedTimestamp: Long
  )

  case class UpdateCreditLimitEvent(accountId: Int, newCreditLimit: Int)
  case class SubmitPaymentEvent(accountId: Int, amount: Int, source: String)

  sealed trait PaymentEvent {
    val accountId: Int
    val paymentId: String
  }

  case class PaymentReturnedEvent(
      accountId: Int,
      paymentId: String,
      amount: Int,
      reason: String,
      recordedTimestamp: Long
  ) extends PaymentEvent

  case class PaymentSubmittedEvent(
      accountId: Int,
      paymentId: String,
      amount: Int,
      recordedTimestamp: Long
  ) extends PaymentEvent

  case class TransactionDecisionedEvent(
      accountId: Int,
      cardNumber: Long,
      transactionId: String,
      amount: Int,
      decision: String,
      declineReason: String,
      ruleVersion: String,
      createdOn: Long
  )

  object AccountCreatedEvent {
    def fromJson(
        json: String
    ): Either[Throwable, EventEnvelope[AccountCreatedEvent]] = {
      Try(mapper.readValue[EventEnvelope[AccountCreatedEvent]](json)).toEither
    }
  }

  object AccountCreditLimitUpdatedEvent {
    def fromJson(
        json: String
    ): Either[Throwable, EventEnvelope[AccountCreditLimitUpdatedEvent]] = {
      Try(
        mapper.readValue[EventEnvelope[AccountCreditLimitUpdatedEvent]](json)
      ).toEither
    }
  }

  object SubmitPaymentEvent {
    def fromJson(
        json: String
    ): Either[Throwable, EventEnvelope[SubmitPaymentEvent]] = {
      Try(
        mapper.readValue[EventEnvelope[SubmitPaymentEvent]](json)
      ).toEither
    }
  }

  object UpdateCreditLimitEvent {
    def fromJson(
        json: String
    ): Either[Throwable, EventEnvelope[UpdateCreditLimitEvent]] = {
      Try(
        mapper.readValue[EventEnvelope[UpdateCreditLimitEvent]](json)
      ).toEither
    }
  }

  object PaymentSubmittedEvent {
    def fromJson(
        json: String
    ): Either[Throwable, EventEnvelope[PaymentSubmittedEvent]] = {
      Try(mapper.readValue[EventEnvelope[PaymentSubmittedEvent]](json)).toEither
    }
  }

  object PaymentReturnedEvent {
    def fromJson(
        json: String
    ): Either[Throwable, EventEnvelope[PaymentReturnedEvent]] = {
      Try(mapper.readValue[EventEnvelope[PaymentReturnedEvent]](json)).toEither
    }
  }
}
