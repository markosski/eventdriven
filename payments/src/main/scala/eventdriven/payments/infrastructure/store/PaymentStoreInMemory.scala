package eventdriven.payments.infrastructure.store

import eventdriven.payments.domain.Payment
import eventdriven.payments.usecases.store.PaymentStore

import scala.collection.mutable

class PaymentStoreInMemory(data: mutable.ListBuffer[Payment]) extends PaymentStore {
  def getAll(accountId: Int): Either[Throwable, List[Payment]] = {
    Right(data.toList)
  }
  def store(payment: Payment): Either[Throwable, Unit] = {
    data.append(payment)
    Right(())
  }
}
