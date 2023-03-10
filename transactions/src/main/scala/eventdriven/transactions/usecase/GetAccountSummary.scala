package eventdriven.transactions.usecase

import eventdriven.transactions.domain.events.TransactionEvent
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.TransactionAggregate
import eventdriven.transactions.domain.entity.transaction.TransactionSummary
import eventdriven.transactions.usecase.store.AccountInfoStore

object GetAccountSummary {
  def apply(accountId: Int)(es: EventStore[TransactionEvent], accountStore: AccountInfoStore): Either[Throwable, TransactionSummary] = {
    for {
      events <- es.get(accountId)
      accountInfo <- accountStore.get(accountId).toRight(new Exception("account does not exist"))
      balance <- new TransactionAggregate(events).getState match {
        case Some(xs) => Right(xs)
        case None => Left(new Exception(s"no transaction data for account $accountId"))
      }
    } yield TransactionSummary(
      accountId,
      balance.balance,
      balance.pending,
      accountInfo.creditLimit - balance.balance - balance.pending
    )
  }
}
