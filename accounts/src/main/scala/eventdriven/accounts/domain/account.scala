package eventdriven.accounts.domain

object account {
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
