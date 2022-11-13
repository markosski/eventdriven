package eventdriven.payments.domain

object PaymentSource extends Enumeration {
  val SAVINGS, CHECKING = Value
}

case class Payment(accountId: Int, paymentId: String, amount: Int, source: PaymentSource.Value, recordedTimestamp: Long)
