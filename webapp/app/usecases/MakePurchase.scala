package usecases

import eventdriven.core.integration.service.transactions.AuthorizationDecisionResponse
import services.TransactionService

import scala.util.Try

object MakePurchase {
  case class MakePurchaseInput(cardNumber: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: String)

  def apply(input: MakePurchaseInput)(implicit transactionService: TransactionService): Either[Throwable, AuthorizationDecisionResponse] = {
    for {
      validCardNumber <- validateCardNumber(input.cardNumber)
      validAmount <- validateAmount(input.amount)
      validMerchantCode <- validateMerchantCode(input.merchantCode)
      validZipCode <- validateZipCode(input.zipOrPostal)
      response <- transactionService.makePurchase(
        validCardNumber,
        validAmount,
        validMerchantCode,
        validZipCode,
        input.countryCode)
    } yield response
  }

  def validateZipCode(zipCode: String): Either[Throwable, String] = {
    if (zipCode.length == 5) Right(zipCode)
    else Left(new Exception("invalid zip code"))
  }

  def validateMerchantCode(merchantCode: String): Either[Throwable, String] = {
    if (merchantCode.nonEmpty) Right(merchantCode)
    else Left(new Exception("merchant code cannot be empty"))
  }

  def validateCardNumber(cardNumber: String): Either[Throwable, Long] = {
    val parsedCardNumber = Try(cardNumber.toLong).toOption
    if (cardNumber.nonEmpty && parsedCardNumber.isDefined) Right(parsedCardNumber.get)
    else Left(new Exception("card number empty or not a number"))
  }

  def validateAmount(amount: Int): Either[Throwable, Int] = {
    if (amount > 0) Right(amount)
    else Left(new Exception("amount cannot be <= 0"))
  }
}
