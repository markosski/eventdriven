package eventdriven.transactions.domain.model

object transaction {
  case class TransactionSummary(accountId: Int, balance: Int)
  case class PreDecisionedTransactionRequest(
                                              cardNumber: Int,
                                              transactionId: String,
                                              amount: Int,
                                              merchantCode: String,
                                              zipOrPostal: String,
                                              countryCode: Int
                                            )
}
