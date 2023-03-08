package eventdriven.payments.usecases

import eventdriven.core.integration.events.PaymentSubmittedEvent
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher, Topics}
import eventdriven.core.util.time
import eventdriven.payments.domain.{Payment, PaymentSource}
import eventdriven.core.util.string
import eventdriven.core.util.json
import eventdriven.payments.usecases.service.TransactionService
import eventdriven.payments.usecases.store.PaymentStore
import wvlet.log.LogSupport

import scala.util.Try

object SubmitPayment extends LogSupport {
  case class SubmitPaymentInput(accountId: String, amount: Int, source: String)

  def apply(input: SubmitPaymentInput)
           (implicit
            store: PaymentStore,
            transactionService: TransactionService,
            dispatcher: EventPublisher[String]): Either[Throwable, String] = for {
    validAccountId <- validate(input.accountId)
    balance        <- transactionService.getBalance(validAccountId) match {
      case Left(err) =>
        info(s"Failure when calling transactions app, will fallback to avg of latest payments: $err")
        store.getAll(validAccountId).map(x => fallback(x))
      case Right(result) => Right(result.balance)
    }
    _              = info(s"Established existing balance is: $balance")
    validAmount    <- validateAmount(balance, input.amount)
    payment        = Payment(validAccountId, string.getUUID(), validAmount, PaymentSource.withName(input.source), time.unixTimestampNow())
    _              <- store.store(payment)
    _              = info(s"Payment stored: $payment")
    payload = PaymentSubmittedEvent(validAccountId, payment.paymentId, payment.amount, payment.recordedTimestamp)
    event          = EventEnvelope(string.getUUID(), Topics.PaymentSubmittedV1.toString, time.unixTimestampNow(), payload)
    _              <- dispatcher.publish(event.id, json.anyToJson(event), Topics.PaymentSubmittedV1.toString)
  } yield payment.paymentId

  def validate(accountId: String): Either[Throwable, Int] = {
    Try(accountId.toInt).toEither
  }

  def validateAmount(balance: Int, paymentAmount: Int): Either[Throwable, Int] = {
    if (paymentAmount > balance)
      Left(new Exception("Amount cannot exceed current balance"))
    else
      Right(paymentAmount)
  }

  def fallback(payments: List[Payment]): Int = {
    if (payments.isEmpty)
      0
    else
      payments.foldLeft(0)( (sum, p) => sum + p.amount) / payments.size
  }
}
