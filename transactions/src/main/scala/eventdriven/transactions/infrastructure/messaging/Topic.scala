package eventdriven.transactions.infrastructure.messaging

object Topic extends Enumeration {
  val ProcessTransaction = Value(1, "processTransaction")
  val TransactionDecisioned = Value(2, "transactionDecisioned")
  val TransactionPaymentApplied = Value(3, "transactionPaymentApplied")
  val TransactionPaymentReturned = Value(4, "transactionPaymentReturned")
  val PaymentSubmitted = Value(5, "paymentSubmitted")
  val PaymentReturned = Value(6, "paymentReturned")
  val AccountCreditLimitUpdated = Value(7, "accountCreditLimitUpdated")
}
