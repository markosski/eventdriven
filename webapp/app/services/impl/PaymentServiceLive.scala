package services.impl

import domain.payment.Payment
import infrastructure.web.AppConfig.PaymentServiceConfig
import services.PaymentService
import sttp.client3._
import util.json

import scala.util.Try

class PaymentServiceLive(config: PaymentServiceConfig) extends PaymentService {
  private val backend = HttpClientSyncBackend()
  def getPayments(accountId: Int): Either[Throwable, List[Payment]] = {
    val request = basicRequest.get(uri"${config.host}:${config.port}/payments/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      accountEntity <- deserialize(body)
    } yield accountEntity
  }

  private def deserialize(jsonString: String): Either[Throwable, List[Payment]] = {
    Try(json.mapper.readValue[List[Payment]](jsonString)).toEither
  }

  def makePayment(accountId: Int, amount: Int, source: String): Either[Throwable, String] = {
    val payload = Map(
      "amount" -> amount,
      "source" -> source
    )
    val request = basicRequest
      .body(json.mapper.writeValueAsString(payload))
      .post(uri"${config.host}:${config.port}/payments/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      response <- Try(json.mapper.readValue[Map[String, String]](body)).toEither
      paymentId = response("response")
    } yield paymentId
  }
}
