package controllers

import infrastructure.AppConfig

import javax.inject._
import play._
import play.api.mvc._
import pureconfig.ConfigSource
import services.impl.{AccountServiceLive, PaymentServiceLive, TransactionServiceLive}
import usecases.{GetAccountInfo, GetBalance, GetTransactions, MakePayment, MakePurchase, UpdateCreditLimit}
import pureconfig._
import pureconfig.generic.auto._
import usecases.MakePurchase.MakePurchaseInput
import wvlet.log.LogSupport

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with LogSupport {
  val config = ConfigSource.resources("app.conf").load[AppConfig] match {
    case Left(err) => throw new Exception(err.toString())
    case Right(config) => config
  }
  implicit val transactionService = new TransactionServiceLive(config.transactionServiceConfig)
  implicit val accountService = new AccountServiceLive(config.accountServiceConfig)
  implicit val paymentService = new PaymentServiceLive(config.paymentServiceConfig)
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def account() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.account())
  }

  def transactions() = Action { implicit request: Request[AnyContent] =>
    (for {
      transactions <- GetTransactions(123)
      account <- GetAccountInfo(123)
      balance <- GetBalance(123)
    } yield (transactions, account, balance)) match {
      case Right(all) => Ok(views.html.transactions(all._1.transactions, all._2, all._3))
      case Left(err) => Ok(views.html.error(err.getMessage))
    }
  }

  def makePayment() = Action { implicit request: Request[AnyContent] =>
    request.method match {
      case "GET" => Ok(views.html.makePayment(None))
      case "POST" => {
        val parsedBody = request.body.asFormUrlEncoded.get
        val accountId = parsedBody.getOrElse("accountId", List("")).map(_.toInt).head
        val amount = parsedBody.getOrElse("amount", List("0"))
          .filter(_.nonEmpty)
          .map(_.toDouble * 100)
          .headOption.getOrElse(0.0).toInt
        val source = parsedBody.getOrElse("source", List("")).head
        val resp = MakePayment(accountId, amount, source) match {
          case Right(r) => Right(r)
          case Left(l) => Left(l.getMessage)
        }
        Ok(views.html.makePayment(Some(resp)))
      }
    }
  }

  def makePurchase() = Action { implicit request: Request[AnyContent] =>
    request.method match {
      case "GET" => Ok(views.html.makePurchase(None))
      case "POST" => {
        val parsedBody = request.body.asFormUrlEncoded.get
        info(s"Submitted purchase $parsedBody")
        val makePurchaseInput = MakePurchaseInput(
          parsedBody.getOrElse("cardNumber", List("")).head,
          (parsedBody.getOrElse("amount", List("0"))
            .filter(_.nonEmpty)
            .map(_.toDouble)
            .headOption.getOrElse(0.0) * 100).toInt,
          parsedBody.getOrElse("merchantCode", List("")).head,
          parsedBody.getOrElse("zipOrPostal", List("")).head,
          parsedBody.getOrElse("countryCode", List("")).head
        )
        val resp = MakePurchase(makePurchaseInput) match {
          case Right(r) => Right(r)
          case Left(l) => Left(l.getMessage)
        }

        Ok(views.html.makePurchase(Some(resp)))
      }
      case _ => Ok(views.html.error("bad request"))
    }
  }

  def admin() = Action { implicit request: Request[AnyContent] =>
    request.method match {
      case "GET" => {
        (for {
          account <- GetAccountInfo(123)
        } yield account) match {
          case Right(account) => Ok(views.html.admin(None, Some(account.creditLimit.toDouble / 100)))
          case Left(err) => Ok(views.html.error(err.getMessage))
        }
      }
      case "POST" => {
        val parsedBody = request.body.asFormUrlEncoded.get
        info(s"Submitted credit update: $parsedBody")
        val accountId = parsedBody.getOrElse("accountId", List("0")).map(_.toInt).head
        val creditLimit = parsedBody.getOrElse("creditLimit", List("0.0")).map(_.toDouble).head
        val resp = UpdateCreditLimit(
          accountId,
          (creditLimit * 100).toInt
        ) match {
          case Right(r) => Right(r)
          case Left(l) => Left(l.getMessage)
        }
        Ok(views.html.admin(Some(resp), Some(creditLimit)))
      }
    }
  }
}
