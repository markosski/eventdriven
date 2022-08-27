package eventdriven.payments.infrastructure.store

import eventdriven.payments.domain.model.payment._

trait PaymentStore {
    def save(payment: PaymentProcessed): Either[Throwable, Unit]
    def get(accountId: Int, asOfTimestamp: Long): List[PaymentProcessed]
}
