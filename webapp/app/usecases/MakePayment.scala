package usecases

import services.PaymentService

object MakePayment {
  def apply(accountId: Int, amount: Int, source: String)(implicit paymentService: PaymentService): Either[Throwable, String] = {
    for {
      validAccountId <- validateAccountId(accountId)
      validAmount <- validateAmount(amount)
      validSource <- validateSource(source)
      response <- paymentService.makePayment(validAccountId, validAmount, validSource)
    } yield response.paymentId
  }

  def validateAccountId(accountId: Int): Either[Throwable, Int] = {
    if (accountId > 0) Right(accountId)
    else Left(new Exception("bad account id"))
  }

  def validateAmount(amount: Int): Either[Throwable, Int] = {
    if (amount > 0) Right(amount)
    else Left(new Exception("amount cannot be <= 0"))
  }

  def validateSource(source: String): Either[Throwable, String] = {
    if (source.nonEmpty) Right(source)
    else Left(new Exception("source cannot be empty"))
  }
}
