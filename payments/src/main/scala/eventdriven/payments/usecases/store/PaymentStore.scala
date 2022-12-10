package eventdriven.payments.usecases.store

import eventdriven.payments.domain.Payment

trait PaymentStore {
  def getAll(accountId: Int): Either[Throwable, List[Payment]]
  def store(payment: Payment): Either[Throwable, Unit]
}
