package eventdriven.core.integration.service.accounts

object GetAccountResponse {
  case class Address(streetAddress: String, zipOrPostal: String, countryCode: String)
}
case class GetAccountResponse(
                    accountId: Int,
                    cardNumber: String,
                    creditLimit: Int,
                    fullName: String,
                    address: GetAccountResponse.Address,
                    phoneNumber: String
                  )
