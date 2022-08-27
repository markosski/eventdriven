package eventdriven.transactions.domain.usecase

import eventdriven.core.infrastructure.store.Cache
import eventdriven.transactions.domain.event.Event
import eventdriven.transactions.domain.event.payment.{PaymentEvent, PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.infrastructure.store.PaymentSummaryStore

import scala.util.Try

/**
 * We want to deduplicate payment events and ensure we are not applying same event twice between transaction snapshots
 */
object ProcessPaymentEvent {
  def apply[T <: PaymentEvent](event: Event[T], startFromTimestamp: Long)(cache: Cache[PaymentEvent], store: PaymentSummaryStore): Either[Throwable, Unit] = event.payload match {
    case e: PaymentReturned => {
      if (cache.exists(event.eventId))
        Left(new Exception(s"payment ${event.eventId} already processed; ignore"))
      else if (e.recordedTimestamp < startFromTimestamp)
        Left(new Exception(s"payment ${event.eventId} was recorded before startFromTimestamp; ignore"))
      else
        store.modify(event.payload.accountId, -e.amount, e.recordedTimestamp)
          .flatMap(_ => Try(cache.put(event.eventId, e)).toEither)
    }
    case e: PaymentSubmitted => {
      if (cache.exists(event.eventId))
        Left(new Exception(s"payment ${event.eventId} already processed; ignore"))
      else if (e.recordedTimestamp < startFromTimestamp)
        Left(new Exception(s"payment ${event.eventId} was recorded before startFromTimestamp; ignore"))
      else
        store.modify(event.payload.accountId, e.amount, e.recordedTimestamp)
          .flatMap(_ => Try(cache.put(event.eventId, e)).toEither)
    }
  }
}
