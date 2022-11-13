package eventdriven.payments.usecases

import eventdriven.core.infrastructure.messaging.events.{PaymentSubmittedEvent}
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.util.time
import eventdriven.payments.domain.{Payment, PaymentSource}
import eventdriven.core.util.string
import eventdriven.core.util.json
import eventdriven.payments.usecases.store.PaymentStore

import scala.util.Try

object SubmitPayment {
  case class SubmitPaymentInput(accountId: String, amount: Int, source: String)

  def apply(input: SubmitPaymentInput)
           (implicit
            store: PaymentStore,
            dispatcher: EventPublisher[String]): Either[Throwable, String] = for {
    validAccountId <- validate(input.accountId)
    payment        = Payment(validAccountId, string.getUUID(), input.amount, PaymentSource.withName(input.source), time.unixTimestampNow())
    _              <- store.store(payment)
    payload = PaymentSubmittedEvent(validAccountId, payment.paymentId, payment.amount, payment.recordedTimestamp)
    event          = EventEnvelope(string.getUUID(), Topics.PaymentSubmittedV1.toString, time.unixTimestampNow(), payload)
    _              <- dispatcher.publish(event.id, json.anyToJson(event), Topics.PaymentSubmittedV1.toString)
  } yield payment.paymentId

  def validate(accountId: String): Either[Throwable, Int] = {
    Try(accountId.toInt).toEither
  }
}
