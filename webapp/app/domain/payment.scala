package domain

object payment {
  case class Payment(accountId: Int, paymentId: String, amount: Int, source: String, recordedTimestamp: Long)
}
