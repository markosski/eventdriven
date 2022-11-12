package eventdriven.payments.infrastructure.store

import eventdriven.payments.domain.Payment
import eventdriven.payments.usecases.PaymentStore

import scala.collection.mutable

class PaymentStoreInMemory(data: mutable.ListBuffer[Payment]) extends PaymentStore {
  def store(payment: Payment): Either[Throwable, Unit] = {
    data.append(payment)
    Right(())
  }
}
