package eventdriven.payments.usecases.store

import eventdriven.payments.domain.Payment

trait PaymentStore {
  def store(payment: Payment): Either[Throwable, Unit]
}
