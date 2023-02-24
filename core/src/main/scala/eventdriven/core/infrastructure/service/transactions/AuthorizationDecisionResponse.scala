package eventdriven.core.infrastructure.service.transactions

case class AuthorizationDecisionResponse(cardNumber: Long, transactionId: String, amount: Int, decision: String)
