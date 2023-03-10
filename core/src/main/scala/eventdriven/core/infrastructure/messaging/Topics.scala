package eventdriven.core.infrastructure.messaging

object Topics extends Enumeration {
  val PaymentSubmittedV1 = Value("paymentSubmitted.v1")
  val PaymentReturnedV1 = Value("paymentReturned.v1")
  val TransactionDecisionedV1 = Value("transactionDecisioned.v1")
  val AccountCreditLimitUpdatedV1 = Value("accountCreditLimitUpdated.v1")
}
