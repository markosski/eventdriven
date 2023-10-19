package usecases

import eventdriven.core.infrastructure.messaging.{EventEnvelope, Topics}
import eventdriven.core.infrastructure.messaging.kafka.KafkaEventProducer
import eventdriven.core.integration.events.UpdateCreditLimitEvent
import eventdriven.core.util.{json, string, time}
import wvlet.log.LogSupport

object UpdateCreditLimit extends LogSupport {
  def apply(accountId: Int, newCreditLimit: Int)(implicit
      eventProducer: KafkaEventProducer
  ): Either[Throwable, Unit] = {
    for {
      _ <- validateAccountId(accountId)
      _ <- validateCreditLimit(newCreditLimit)
      payload = UpdateCreditLimitEvent(accountId, newCreditLimit)
      envelope = EventEnvelope(
        string.getUUID(),
        Topics.UpdateCreditLimitV1.toString,
        time.unixTimestampNow(),
        payload
      )
      _ <- eventProducer.publish(
        payload.accountId.toString,
        json.anyToJson(envelope),
        envelope.eventType
      )
      _ = info(s"Published event $envelope")
    } yield ()
  }

  def validateAccountId(accountId: Int): Either[Throwable, Int] = {
    if (accountId > 0) Right(accountId)
    else Left(new Exception("account id must be > 0"))
  }

  def validateCreditLimit(newCreditLimit: Int): Either[Throwable, Int] = {
    if (newCreditLimit > 0) Right(newCreditLimit)
    else Left(new Exception("credit limit cannot be <= 0"))
  }
}
