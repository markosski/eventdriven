package eventdriven.transactions.infrastructure.store

import eventdriven.transactions.domain.model.payment.PaymentSummary
import wvlet.log.LogSupport

import scala.collection.mutable
import scala.util.Try

class PaymentSummaryStoreInMemory(data: mutable.ListBuffer[PaymentSummary]) extends PaymentSummaryStore with LogSupport {
  def get(accountId: Int): Option[PaymentSummary] = {
    data.find(_.accountId == accountId)
  }
  def modify(accountId: Int, amount: Int, timestamp: Long): Either[Throwable, Unit] = Try {
    info("modifying payment summary")
    val idx = data.indexWhere(_.accountId == accountId)
    if (idx >= 0) {
      val item = data(idx)
      val newPaymentAmount = item.totalAmountInCents + amount
      info(s"new payment amount $newPaymentAmount")

      data(idx) = item.copy(
        totalAmountInCents = newPaymentAmount,
        lastPaymentTimestamp = timestamp
      )
    } else {
      data.append(PaymentSummary(accountId, amount, timestamp))
      ()
    }
  }.toEither
}
