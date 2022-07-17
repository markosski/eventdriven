package eventdriven.transactions.infrastructure.store

import eventdriven.transactions.domain.model.payment.PaymentSummary

trait PaymentSummaryStore {
  def get(accountId: Int): Option[PaymentSummary]
  def modify(accountId: Int, amount: Int, timestamp: Long): Either[Throwable, Unit]
}
