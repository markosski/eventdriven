package services

import domain.payment.Payment

trait PaymentService {
  def getPayments(accountId: Int): Either[Throwable, List[Payment]]
  def makePayment(accountId: Int, amount: Int, source: String): Either[Throwable, String]
}
