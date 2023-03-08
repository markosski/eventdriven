package eventdriven.core.integration.service.transactions

case class AuthorizationDecisionResponse(cardNumber: Long, transactionId: String, amount: Int, decision: String)
