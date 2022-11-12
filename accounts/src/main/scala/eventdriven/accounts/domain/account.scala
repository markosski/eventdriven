package eventdriven.accounts.domain

object account {
  case class AccountCreditLimitUpdatedEvent(accountId: Int, newCreditLimit: Int, recordedTimestamp: Long)

  case class Address(streetAddress: String, zipOrPostal: String, countryCode: String)

  case class Account(
                      accountId: Int,
                      cardNumber: String,
                      creditLimit: Int,
                      fullName: String,
                      address: Address,
                      phoneNumber: String
                    )
}
