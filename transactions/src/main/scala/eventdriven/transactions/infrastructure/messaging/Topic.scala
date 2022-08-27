package eventdriven.transactions.infrastructure.messaging

object Topic extends Enumeration {
  val ProcessTransaction = Value(1, "processTransaction")
  val TransactionDecisioned = Value(2, "transactionDecisioned")
  val PaymentSubmitted = Value(3, "paymentSubmitted")
  val PaymentReturned = Value(4, "paymentReturned")
}
