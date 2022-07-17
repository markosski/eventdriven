package eventdriven.transactions.domain.event

case class PreDecisionedAuth(
                              account: Int,
                              card: Int,
                              amount: Int,
                              merchantCode: String,
                              zipOrPostal: String,
                              countryCode: Int
                            )
