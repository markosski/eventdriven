package eventdriven.payments.usecases.service

import eventdriven.payments.domain.entity.transaction.TransactionAccountSummary

trait TransactionService {
  def getBalance(accountId: Int): Either[Throwable, TransactionAccountSummary]
}