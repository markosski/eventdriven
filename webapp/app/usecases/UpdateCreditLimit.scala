package usecases

import services.AccountService

object UpdateCreditLimit {
  def apply(accountId: Int, newCreditLimit: Int)(implicit accountService: AccountService): Either[Throwable, Unit] = {
    for {
      validAccountId <- validateAccountId(accountId)
      validCL <- validateCreditLimit(newCreditLimit)
    } yield accountService.updateCreditLimit(validAccountId, validCL)
  }

  def validateAccountId(accountId: Int): Either[Throwable, Int] = {
    if (accountId > 0) Right(accountId)
    else Left(new Exception("account id must b e> 0"))
  }

  def validateCreditLimit(newCreditLimit: Int): Either[Throwable, Int] = {
    if (newCreditLimit > 0) Right(newCreditLimit)
    else Left(new Exception("credit limit cannot be <= 0"))
  }
}
