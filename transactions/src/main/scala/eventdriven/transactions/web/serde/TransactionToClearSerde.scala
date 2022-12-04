package eventdriven.transactions.web.serde

import eventdriven.core.util.json
import eventdriven.transactions.domain.entity.transaction.TransactionToClear

import scala.util.Try

object TransactionToClearSerde {
  def fromJson(jsonString: String): Either[Throwable, List[TransactionToClear]] = {
    Try(json.mapper.readValue[List[TransactionToClear]](jsonString)).toEither
  }

}
