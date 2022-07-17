package eventdriven.transactions.domain.event

trait AccountEvent

object AccountEvent {
  case class AccountCreated(accountId: Int, creditLimit: Int)
  case class AccountCreditLimitUpdated(accountId: Int, oldCreditLimit: Int, newCreditLimit: Int)
}
