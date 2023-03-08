package services.impl

import domain.payment.Payment
import eventdriven.core.integration.service.ErrorResponse
import eventdriven.core.integration.service.payments.{SubmitPaymentRequest, SubmitPaymentResponse}
import infrastructure.web.AppConfig.PaymentServiceConfig
import services.PaymentService
import sttp.client3._
import util.json

import scala.util.Try

class PaymentServiceLive(config: PaymentServiceConfig) extends PaymentService {
  private val backend = HttpClientSyncBackend()

  def getPayments(accountId: Int): Either[Throwable, List[Payment]] = {
    val request = basicRequest.get(uri"${config.hostString}:${config.port}/payments/$accountId")
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
    val payload = SubmitPaymentRequest(amount, source)
    val request = basicRequest
      .body(json.mapper.writeValueAsString(payload))
      .post(uri"${config.hostString}:${config.port}/payments/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      response <- Try(json.mapper.readValue[SubmitPaymentResponse](body))
        .fold(
          _ => Try(json.mapper.readValue[ErrorResponse](body))
            .fold(
              err => Left(err),
              errResp => Left(new Exception(errResp.error))
            ),
          succ => Right(succ.paymentId)
        )
    } yield response
  }
}
