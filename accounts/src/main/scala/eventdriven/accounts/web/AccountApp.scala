package eventdriven.accounts.web

import wvlet.log.LogSupport
import eventdriven.accounts.infrastructure.AppConfig
import eventdriven.accounts.infrastructure.env.local
import eventdriven.accounts.usecase.{GetAccount, UpdateCreditLimit}
import eventdriven.core.integration.service.ErrorResponse
import eventdriven.core.integration.service.accounts.{GetAccountResponse, UpdateCreditLimitRequest}
import eventdriven.core.util.json.anyToJson
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

object AccountApp extends  LogSupport {
  def main(args: Array[String]): Unit = {
    val config = ConfigSource.default.load[AppConfig] match {
      case Left(err) => throw new Exception(err.toString())
      case Right(config) => config
    }
    val environment = local.getEnv(config)
    implicit val accountStore = environment.accountStore
    implicit val outboxPoller = environment.outboxPoller

    implicit val system = ActorSystem(Behaviors.empty, "accounts")
    implicit val executionContext = system.executionContext

    executionContext.execute(() => outboxPoller.run())

    val getHealth =
      path("_health") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, """{"response": "healthy"}"""))
        }
      }
    
    val getAccounts = 
      path("accounts" / """\d+""".r) { 
        accountId =>
          get {
            val resp = GetAccount(accountId) match {
              case Right(account) => 
                anyToJson(
                  GetAccountResponse(
                    account.accountId,
                    account.cardNumber,
                    account.creditLimit,
                    account.fullName,
                    GetAccountResponse.Address(
                      account.address.streetAddress,
                      account.address.zipOrPostal,
                      account.address.countryCode
                    ),
                    account.phoneNumber
                  )
                )
              case Left(err) => ErrorResponse.toJson(err.getMessage)
            }
            complete(HttpEntity(ContentTypes.`application/json`, resp))
          }
      }
    val updateCreditLimit = 
      path("accounts" / """\d+""".r / "creditLimit") {
        accountId => 
          put {
            entity(as[String]) { raw =>
              val payload = UpdateCreditLimitRequest.fromJson(raw)
              val resp = UpdateCreditLimit(accountId, payload.newCreditLimit) match {
                case Right(_) => ""
                case Left(err) => ErrorResponse.toJson(err.getMessage)
              }
              complete(HttpEntity(ContentTypes.`application/json`, resp))
            }
          }
      }
      
    val bindingFuture = Http()
      .newServerAt(
        "localhost", 
        config.webConfig.port)
      .bind(concat(getHealth, getAccounts, updateCreditLimit))
  }
}
