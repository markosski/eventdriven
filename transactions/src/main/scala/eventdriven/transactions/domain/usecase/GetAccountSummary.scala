package eventdriven.transactions.domain.usecase

import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.event.transaction.TransactionEvent
import eventdriven.transactions.domain.model.account.AccountTransactionSummary
import eventdriven.transactions.domain.projection.TransactionSummaryProjection
import eventdriven.transactions.infrastructure.store.{AccountInfoStore}

object GetAccountSummary {
  def apply(accountId: Int)(es: EventStore[TransactionEvent], accountStore: AccountInfoStore): Either[Throwable, AccountTransactionSummary] = {
    for {
      trxs <- es.get(accountId)
      summary <- (new TransactionSummaryProjection(trxs)).get match {
        case Some(xs) => Right(xs)
        case None => Left(new Exception(s"no transaction data for account $accountId"))
      }
      info <- accountStore.get(accountId) match {
        case Some(xs) => Right(xs)
        case None => Left(new Exception(s"no account info data for account $accountId"))
      }
      currentBalane = summary.balance
    } yield AccountTransactionSummary(accountId, info.cardNumber, info.creditLimit, currentBalane, info.zipOrPostal, info.state)
  }
}
