package eventdriven.transactions

import eventdriven.core.infrastructure.messaging.kafka.KafkaConfig.{KafkaConsumerConfig, KafkaProducerConfig}
import eventdriven.core.infrastructure.messaging.kafka.{KafkaEventListener, KafkaEventProducer}
import eventdriven.transactions.domain.event.payment.{PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.domain.event.transaction.{TransactionDecisioned, TransactionEvent}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.payment
import eventdriven.transactions.domain.model.payment.PaymentSummary
import eventdriven.transactions.domain.usecase.{GetAccountSummary, ProcessTransaction}
import eventdriven.transactions.infrastructure.store.{AccountInfoStoreInMemory, PaymentSummaryStore, PaymentSummaryStoreInMemory, TransactionStoreInMemory}
import eventdriven.transactions.infrastructure.web.serde.{ErrorResponseSerde, GetAccountSummarySerde, OkResponseSerde, ProcessTransactionSerde}
import org.scalatra._

import java.util.concurrent.{Executors, ThreadPoolExecutor}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransactionsApp extends ScalatraServlet {
  val esData = mutable.ListBuffer[TransactionEvent]()
  esData.append(TransactionDecisioned(123, "1", 1000, "Approved", "", 1001))
  esData.append(TransactionDecisioned(123, "2", 1099, "Approved", "", 1002))
  esData.append(TransactionDecisioned(123, "3", 2100, "Approved", "", 1003))
  val es = new TransactionStoreInMemory(esData)

  val accountInfoData = mutable.ListBuffer[AccountInfo]()
  accountInfoData.append(AccountInfo(123, 50000, "80126", "CO"))
  val accountInfoStore = new AccountInfoStoreInMemory(accountInfoData)

  val paymentsStore = new PaymentSummaryStoreInMemory(mutable.ListBuffer[PaymentSummary]())

  val kconfig = KafkaProducerConfig(
    "localhost",
    19092,
    "org.apache.kafka.common.serialization.StringSerializer",
    "org.apache.kafka.common.serialization.StringSerializer")
  val dispatcher = new KafkaEventProducer("transactions", kconfig)

  val kconfigConsumer = KafkaConsumerConfig(
    "localhost",
    19092,
    "group1",
    "org.apache.kafka.common.serialization.StringDeserializer",
    "org.apache.kafka.common.serialization.StringDeserializer")

  val paymentSubmittedConsumer = new KafkaEventListener("paymentSubmitted", "transactions", kconfigConsumer)
  val paymentReturnedConsumer = new KafkaEventListener("paymentReturned", "transactions", kconfigConsumer)
//  val accountCreditLineUpdatedConsumer = new KafkaEventListener("accountCreditLineUpdated", "transactions", kconfigConsumer)

  val executor = Executors.newFixedThreadPool(4)

  val paymentSubmittedFunc = new Runnable {
    override def run(): Unit = {
      while(true) {
        paymentSubmittedConsumer.take match {
          case Some(xs) => xs.foreach { json =>
            for {
              payment <- PaymentSubmitted.fromJson(json)
              _ = println(payment)
              _ <- paymentsStore.modify(payment.payload.accountId, payment.payload.amount, payment.eventTimestamp)
            } yield ()
          }
          case None => ()
        }
      }
    }
  }

  val paymentReturnedFunc = new Runnable {
    override def run(): Unit = {
      while(true) {
        paymentReturnedConsumer.take match {
          case Some(xs) => xs.foreach { json =>
            for {
              payment <- PaymentReturned.fromJson(json)
              _ = println(payment)
              _ <- paymentsStore.modify(payment.payload.accountId, -payment.payload.amount, payment.eventTimestamp)
            } yield ()
          }
          case None => ()
        }
      }
    }
  }

//  val accountCreditLimtUpdated = new Runnable {
//    override def run(): Unit = {
//      while(true) {
//        paymentReturnedConsumer.take match {
//          case Some(xs) => xs.foreach { json =>
//            for {
//              payment <- PaymentSubmitted.fromJson(json)
//              _ = println(payment)
//              _ <- paymentsStore.modify(payment.accountId, payment.paymentId, -payment.amount)
//            } yield ()
//          }
//          case None => ()
//        }
//      }
//    }
//  }

  val paymentSubmittedTask = executor.submit(paymentSubmittedFunc)
  val paymentReturnedTask = executor.submit(paymentReturnedFunc)

  post("/process-purchase-transaction") {
    (for {
      preAuth <- ProcessTransactionSerde.fromJson(request.body)
      processed <- ProcessTransaction(preAuth)(es, accountInfoStore, paymentsStore, dispatcher)
    } yield processed) match {
      case Left(err) =>
        err.printStackTrace()
        ErrorResponseSerde.toJson(err.getMessage)
      case Right(_) => OkResponseSerde.toJson("transaction processed")
    }
  }

  get("/account-summary/:accountId") {
    val accountId = Integer.parseInt(params("accountId"))
    GetAccountSummary(accountId)(es, accountInfoStore, paymentsStore) match {
      case Right(obj) => GetAccountSummarySerde.toJson(obj)
      case Left(err) => {
        err.printStackTrace()
        ErrorResponseSerde.toJson(err.getMessage)
      }
    }
  }
}
