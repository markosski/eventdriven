package eventdriven.transactions.infrastructure.messaging

object Topic extends Enumeration {
  val TransactionDecisioned = Value(1, "transactionDecisioned")
}
