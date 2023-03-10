package services.impl

import eventdriven.core.integration.service.transactions.GetTransactionsResponse.{TransactionInfo, TransactionInfoPayment, TransactionInfoPurchase}
import eventdriven.core.integration.service.transactions.{AuthorizationDecisionRequest, AuthorizationDecisionResponse, GetAccountBalanceResponse, GetTransactionsResponse}
import infrastructure.AppConfig.TransactionServiceConfig
import services.TransactionService
import services.impl.TransactionServiceLive.deserializeTransactionInfo
import sttp.client3._
import util.json

import java.util.UUID
import scala.util.Try

object TransactionServiceLive {
  def deserializeTransactionInfo(jsonString: String): Either[Throwable, List[TransactionInfo]] = Try {
    val jsonMap = json.mapper.readValue[Map[String, Any]](jsonString)
    jsonMap("transactions").asInstanceOf[List[Map[String, Any]]].map { x =>
      val t = x("transaction").asInstanceOf[Map[String, Any]]
      x("category") match {
        case "purchase" => TransactionInfo(
          category = "purchase",
          transaction = TransactionInfoPurchase(
            t("accountId").asInstanceOf[Int],
            t("transactionId").asInstanceOf[String],
            t("amount").asInstanceOf[Int],
            t("decision").asInstanceOf[String],
            t("decisionReason").asInstanceOf[String],
            t("createdOn").asInstanceOf[Long])
        )
        case "payment" => TransactionInfo(
          category = "payment",
          transaction = TransactionInfoPayment(
            t("accountId").asInstanceOf[Int],
            t("transactionId").asInstanceOf[String],
            t("amount").asInstanceOf[Int],
            t("createdOn").asInstanceOf[Long])
        )
        case _ => throw new Exception("not recognized transaction category")
      }
    }

  }.toEither
}

class TransactionServiceLive(config: TransactionServiceConfig) extends TransactionService {
  private val backend = HttpClientSyncBackend()

  def getRecentTransactions(accountId: Int): Either[Throwable, GetTransactionsResponse] = {
    val request = basicRequest.get(uri"${config.hostString}:${config.port}/transactions/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      transactions <- deserializeTransactionInfo(body)
    } yield GetTransactionsResponse(transactions)
  }

  def getBalance(accountId: Int): Either[Throwable, GetAccountBalanceResponse] = {
    val request = basicRequest.get(uri"${config.hostString}:${config.port}/balance/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      response <- Try(json.mapper.readValue[GetAccountBalanceResponse](body)).toEither
    } yield GetAccountBalanceResponse(
      response.accountId,
      response.balance,
      response.pending,
      response.available)
  }

  def makePurchase(cardNumber: Long, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: String): Either[Throwable, AuthorizationDecisionResponse] = {
    val payload = AuthorizationDecisionRequest(
      cardNumber,
      UUID.randomUUID().toString,
      amount,
      merchantCode,
      zipOrPostal,
      countryCode.toInt
    )
    val request = basicRequest
      .body(json.mapper.writeValueAsString(payload))
      .post(uri"${config.hostString}:${config.port}/authorize")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      response <- Try(json.mapper.readValue[AuthorizationDecisionResponse](body)).toEither
    } yield AuthorizationDecisionResponse(
      response.cardNumber,
      response.transactionId,
      response.amount,
      response.decision
    )
  }
}
