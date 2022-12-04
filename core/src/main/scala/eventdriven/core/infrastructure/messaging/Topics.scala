package eventdriven.core.infrastructure.messaging

object Topics extends Enumeration {
  val PaymentSubmittedV1 = Value("paymentSubmitted.v1")
  val PaymentReturnedV1 = Value("paymentReturned.v1")
  val ProcessTransactionV1 = Value("processTransaction.v1")
  val TransactionDecisionedV1 = Value("transactionDecisioned.v1")
  val TransactionPaymentAppliedV1 = Value("transactionPaymentApplied.v1")
  val TransactionPaymentReturnedV1 = Value("transactionPaymentReturned.v1")
  val TransactionClearingResponseV1 = Value("transactionClearingCode.v1")
  val AccountCreditLimitUpdatedV1 = Value("accountCreditLimitUpdated.v1")
}
