package services.impl

import domain.account.Account
import infrastructure.web.AppConfig.AccountServiceConfig
import services.AccountService
import sttp.client3._
import util.json

import scala.util.Try

object AccountServiceLive {
}

class AccountServiceLive(config: AccountServiceConfig) extends AccountService {
  private val backend = HttpClientSyncBackend()
  def accountDetails(accountId: Int): Either[Throwable, Account] = {
    val request = basicRequest.get(uri"${config.host}:${config.port}/accounts/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      accountEntity <- deserialize(body)
    } yield accountEntity
  }

  private def deserialize(jsonString: String): Either[Throwable, Account] = {
    Try(json.mapper.readValue[Account](jsonString)).toEither
  }
}
