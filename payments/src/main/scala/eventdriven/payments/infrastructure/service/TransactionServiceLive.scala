package eventdriven.payments.infrastructure.service

import eventdriven.core.util.json
import eventdriven.payments.domain.entity.transaction.TransactionAccountSummary
import eventdriven.payments.infrastructure.AppConfig.TransactionServiceConfig
import eventdriven.payments.usecases.service.TransactionService
import sttp.client3.{HttpClientSyncBackend, UriContext, basicRequest}

import scala.util.Try

class TransactionServiceLive(config: TransactionServiceConfig) extends TransactionService {
  private val backend = HttpClientSyncBackend()

  def getBalance(accountId: Int): Either[Throwable, TransactionAccountSummary] = {
    val request = basicRequest.get(uri"${config.hostString}:${config.port}/balance/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      summary <- Try(json.mapper.readValue[TransactionAccountSummary](body)).toEither
    } yield summary
  }
}
