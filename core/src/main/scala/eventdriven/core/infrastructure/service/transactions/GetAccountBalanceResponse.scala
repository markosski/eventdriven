package eventdriven.core.infrastructure.service.transactions

case class GetAccountBalanceResponse(accountId: Int, balance: Int, pending: Int, available: Int)
