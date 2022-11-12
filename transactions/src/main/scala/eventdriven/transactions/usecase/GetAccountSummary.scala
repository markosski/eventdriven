package eventdriven.transactions.usecase

import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.event.transaction.TransactionEvent
import eventdriven.transactions.usecase.projection.TransactionSummaryProjection

object GetAccountSummary {
  def apply(accountId: Int)(es: EventStore[TransactionEvent]): Either[Throwable, Int] = {
    for {
      events <- es.get(accountId)
      summary <- new TransactionSummaryProjection(events).get match {
        case Some(xs) => Right(xs)
        case None => Left(new Exception(s"no transaction data for account $accountId"))
      }
    } yield summary.balance
  }
}
