package eventdriven.transactions.domain.model

object payment {
  case class PaymentSummary(accountId: Int, totalAmountInCents: Int, lastPaymentTimestamp: Long)
}
