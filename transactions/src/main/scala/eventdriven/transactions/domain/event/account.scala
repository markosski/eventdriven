package eventdriven.transactions.domain.event

object account {
  case class AccountCreated(accountId: Int, cardNumber: Long, creditLimit: Int, recordedTimestamp: Long)
  case class AccountCreditLimitUpdated(accountId: Int, oldCreditLimit: Int, newCreditLimit: Int, recordedTimestamp: Long)
}
