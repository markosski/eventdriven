package usecases

import eventdriven.core.infrastructure.messaging.{EventEnvelope, Topics}
import eventdriven.core.infrastructure.messaging.kafka.KafkaEventProducer
import eventdriven.core.integration.events.SubmitPaymentEvent
import eventdriven.core.util.{json, string, time}
import wvlet.log.LogSupport

object MakePayment extends LogSupport {
  def apply(accountId: Int, amount: Int, source: String)(implicit
      eventPublisher: KafkaEventProducer
  ): Either[Throwable, String] = {
    for {
      validAccountId <- validateAccountId(accountId)
      validAmount <- validateAmount(amount)
      validSource <- validateSource(source)
      payload = SubmitPaymentEvent(validAccountId, validAmount, validSource)
      envelope = EventEnvelope(
        string.getUUID(),
        Topics.SubmitPaymentV1.toString,
        time.unixTimestampNow(),
        payload
      )
      _ <- eventPublisher.publish(
        payload.accountId.toString,
        json.anyToJson(envelope),
        envelope.eventType
      )
      _ = info(s"Published event $envelope")
    } yield envelope.id
  }

  def validateAccountId(accountId: Int): Either[Throwable, Int] = {
    if (accountId > 0) Right(accountId)
    else Left(new Exception("bad account id"))
  }

  def validateAmount(amount: Int): Either[Throwable, Int] = {
    if (amount > 0) Right(amount)
    else Left(new Exception("amount cannot be <= 0"))
  }

  def validateSource(source: String): Either[Throwable, String] = {
    if (source.nonEmpty) Right(source)
    else Left(new Exception("source cannot be empty"))
  }
}
