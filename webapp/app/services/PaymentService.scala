package services

import domain.payment.Payment
import eventdriven.core.infrastructure.serde.payments.SubmitPaymentResponse

trait PaymentService {
  def getPayments(accountId: Int): Either[Throwable, List[Payment]]
  def makePayment(accountId: Int, amount: Int, source: String): Either[Throwable, SubmitPaymentResponse]
}
