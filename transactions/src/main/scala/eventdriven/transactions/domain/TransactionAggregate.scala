package eventdriven.transactions.domain

import eventdriven.core.integration.events.{PaymentReturnedEvent, PaymentSubmittedEvent}
import eventdriven.core.integration.service.transactions.{AuthorizationDecisionRequest, TransactionToClear}
import eventdriven.core.models.Aggregate
import eventdriven.core.util.time
import eventdriven.transactions.domain.entity.account.AccountInfo
import eventdriven.transactions.domain.entity.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.entity.transaction.TransactionBalance
import eventdriven.transactions.domain.events._
import wvlet.log.LogSupport

class TransactionAggregate(events: List[TransactionEvent]) extends Aggregate[Int, TransactionBalance, TransactionEvent] with LogSupport {
  private val balanceProjection = new TransactionBalanceProjection(events)

  override lazy val getState: Option[TransactionBalance] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      balanceProjection.get
    }
  }

  def processAuthorization(preAuth: AuthorizationDecisionRequest, accountInfo: AccountInfo): Either[Throwable, TransactionDecisionedEvent] = {
    if (events.collect { case t: TransactionDecisionedEvent => t}.exists(_.transactionId == preAuth.transactionId)) {
      Left(new Exception(s"transaction id ${preAuth.transactionId} has been already processed"))
    } else {
      val decision = for {
        trxSummary <- getState
      } yield rules.current.run(preAuth, trxSummary, accountInfo)

      decision match {
        case None => Left(new Exception("could not decision, missing account data"))
        case Some(DecisionResult(Decision.Approved, version, _)) => Right(
          TransactionDecisionedEvent(
            accountInfo.accountId,
            preAuth.cardNumber,
            preAuth.transactionId,
            preAuth.amount,
            Decision.Approved,
            "",
            version,
            time.unixTimestampNow()))
        case Some(DecisionResult(Decision.Declined, version, Some(reason))) =>
          Right(
            TransactionDecisionedEvent(
              accountInfo.accountId,
              preAuth.cardNumber,
              preAuth.transactionId,
              preAuth.amount,
              Decision.Declined,
              reason,
              version,
              time.unixTimestampNow()))
        case _ => Left(new Exception("unsupported decision result"))
      }
    }
  }

  def clearTransactions(transactionToClear: TransactionToClear): Either[Throwable, TransactionClearingResultEvent] = {
    val transaction = events.collect { case e: TransactionDecisionedEvent => e }.filter(_.transactionId == transactionToClear.transactionId)
    val isClearedAlready = events.collect { case e: TransactionClearingResultEvent => e }.exists(_.transactionId == transactionToClear.transactionId)

    if (isClearedAlready) {
      Left(new Exception(s"Transaction ${transactionToClear.transactionId} already cleared"))
    } else {
      transaction.headOption.map { t =>
        val clearingDecision = clearing.clearTransaction(t, transactionToClear)
        Right(TransactionClearingResultEvent(t.accountId, t.transactionId, t.amount, clearingDecision, time.unixTimestampNow()))
      } match {
        case Some(x) => x
        case None => Left(new Exception(s"Transaction ${transactionToClear.transactionId} not found"))
      }
    }
  }

  def applyPayment(payment: PaymentSubmittedEvent): Either[Throwable, TransactionPaymentAppliedEvent] = {
    if (events.collect { case p: TransactionPaymentAppliedEvent => p}.exists(_.paymentId == payment.paymentId)) {
      Left(new Exception(s"payment submitted id ${payment.paymentId} has been already processed"))
    } else {
      Right(TransactionPaymentAppliedEvent(payment.accountId, payment.paymentId, payment.amount, time.unixTimestampNow()))
    }
  }

  def applyReturnedPayment(payment: PaymentReturnedEvent): Either[Throwable, TransactionPaymentReturnedEvent] = {
    if (events.collect { case p: TransactionPaymentReturnedEvent => p}.exists(_.paymentId == payment.paymentId)) {
      Left(new Exception(s"payment returned id ${payment.paymentId} has been already processed"))
    } else {
      Right(TransactionPaymentReturnedEvent(payment.accountId, payment.paymentId, payment.amount, time.unixTimestampNow()))
    }
  }
}
