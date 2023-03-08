package eventdriven.core.integration.service.transactions

case class GetAccountBalanceResponse(accountId: Int, balance: Int, pending: Int, available: Int)
