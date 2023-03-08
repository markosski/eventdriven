package eventdriven.core.integration.service.transactions

import eventdriven.core.util.json

import scala.util.Try

case class TransactionToClear(accountId: Int, transactionId: String, amount: Int)

object ClearTransactionsRequest {
  def fromJson(jsonString: String): Either[Throwable, List[TransactionToClear]] = {
    Try(json.mapper.readValue[List[TransactionToClear]](jsonString)).toEither
  }
}
