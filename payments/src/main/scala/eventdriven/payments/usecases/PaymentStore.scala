package eventdriven.payments.usecases

import eventdriven.payments.domain.Payment

trait PaymentStore {
  def store(payment: Payment): Either[Throwable, Unit]
}
