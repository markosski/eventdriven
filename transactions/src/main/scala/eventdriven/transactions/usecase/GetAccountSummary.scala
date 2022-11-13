package eventdriven.transactions.usecase

import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.event.transaction.TransactionEvent
import eventdriven.transactions.domain.model.transaction.TransactionSummary
import eventdriven.transactions.usecase.projection.TransactionBalanceProjection
import eventdriven.transactions.usecase.store.AccountInfoStore

object GetAccountSummary {
  def apply(accountId: Int)(es: EventStore[TransactionEvent], accountStore: AccountInfoStore): Either[Throwable, TransactionSummary] = {
    for {
      events <- es.get(accountId)
      accountInfo <- accountStore.get(accountId).toRight(new Exception("account does not exist"))
      balance <- new TransactionBalanceProjection(events).get match {
        case Some(xs) => Right(xs)
        case None => Left(new Exception(s"no transaction data for account $accountId"))
      }
    } yield TransactionSummary(accountId, balance.balance, accountInfo.creditLimit - balance.balance)
  }
}
