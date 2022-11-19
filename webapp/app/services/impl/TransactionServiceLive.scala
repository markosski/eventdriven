package services.impl

import domain.transaction.{DecisionedTransactionResponse, TransactionAccountSummary, TransactionInfo, TransactionInfoPayment, TransactionInfoPurchase}
import infrastructure.web.AppConfig.TransactionServiceConfig
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
            t("createdOn").asInstanceOf[Int])
        )
        case "payment" => TransactionInfo(
          category = "payment",
          transaction = TransactionInfoPayment(
            t("accountId").asInstanceOf[Int],
            t("transactionId").asInstanceOf[String],
            t("amount").asInstanceOf[Int],
            t("createdOn").asInstanceOf[Int])
        )
        case _ => throw new Exception("not recognized transaction category")
      }
    }

  }.toEither
}

class TransactionServiceLive(config: TransactionServiceConfig) extends TransactionService {
  private val backend = HttpClientSyncBackend()

  def getRecentTransactions(accountId: Int): Either[Throwable, List[TransactionInfo]] = {
    val request = basicRequest.get(uri"${config.hostString}:${config.port}/transactions/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      transactions <- deserializeTransactionInfo(body)
    } yield transactions
  }

  def getBalance(accountId: Int): Either[Throwable, TransactionAccountSummary] = {
    val request = basicRequest.get(uri"${config.hostString}:${config.port}/balance/$accountId")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      summary <- Try(json.mapper.readValue[TransactionAccountSummary](body)).toEither
    } yield summary
  }

  def makePurchase(cardNumber: String, amount: Int, merchantCode: String, zipOrPostal: String, countryCode: String): Either[Throwable, DecisionedTransactionResponse] = {
    val payload = Map(
      "cardNumber" -> cardNumber,
      "transactionId" -> UUID.randomUUID().toString,
      "amount" -> amount.toString,
      "merchantCode" -> merchantCode,
      "zipOrPostal" -> zipOrPostal,
      "countryCode" -> countryCode
    )
    val request = basicRequest
      .body(json.mapper.writeValueAsString(payload))
      .post(uri"${config.hostString}:${config.port}/process-purchase-transaction")
    val response = request.send(backend)
    for {
      body <- response.body.fold[Either[Throwable, String]](x => Left(new Exception(x)), x => Right(x))
      response <- Try(json.mapper.readValue[DecisionedTransactionResponse](body)).toEither
    } yield response
  }
}
