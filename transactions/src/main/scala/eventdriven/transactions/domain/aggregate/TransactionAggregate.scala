package eventdriven.transactions.domain.aggregate

import eventdriven.core.domain.Aggregate
import eventdriven.core.domain.events.{PaymentReturnedEvent, PaymentSubmittedEvent, TransactionClearingResultEvent, TransactionDecisionedEvent, TransactionEvent, TransactionPaymentAppliedEvent, TransactionPaymentReturnedEvent}
import eventdriven.core.infrastructure.service.transactions.{AuthorizationDecisionRequest, TransactionToClear}
import eventdriven.core.util.time
import eventdriven.transactions.domain.clearing.Clearing
import eventdriven.transactions.domain.decisioning.Rules
import eventdriven.transactions.domain.entity.account.AccountInfo
import eventdriven.transactions.domain.entity.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.entity.transaction.TransactionBalance
import eventdriven.transactions.domain.projection.TransactionBalanceProjection
import wvlet.log.LogSupport

class TransactionAggregate(events: List[TransactionEvent]) extends Aggregate[Int, TransactionBalance, TransactionEvent] with LogSupport {
  val balanceProjection = new TransactionBalanceProjection(events)
  override def buildState: Option[TransactionBalance] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      balanceProjection.get
    }
  }

  def handle(preAuth: AuthorizationDecisionRequest, accountInfo: AccountInfo): Either[Throwable, TransactionDecisionedEvent] = {
    if (events.collect { case t: TransactionDecisionedEvent => t}.exists(_.transactionId == preAuth.transactionId)) {
      Left(new Exception(s"transaction id ${preAuth.transactionId} has been already processed"))
    } else {
      val decision = for {
        trxSummary <- buildState
      } yield Rules.current.run(preAuth, trxSummary, accountInfo)

      decision match {
        case None => Left(new Exception("could not decision, missing account data"))
        case Some(DecisionResult(Decision.Approved, version, _)) => Right(
          TransactionDecisionedEvent(
            accountInfo.accountId,
            preAuth.cardNumber,
            preAuth.transactionId,
            preAuth.amount,
            Decision.Approved.toString,
            "",
            version,
            1234))
        case Some(DecisionResult(Decision.Declined, version, Some(reason))) =>
          Right(
            TransactionDecisionedEvent(
              accountInfo.accountId,
              preAuth.cardNumber,
              preAuth.transactionId,
              preAuth.amount,
              Decision.Declined.toString,
              reason,
              version,
              1234))
        case _ => Left(new Exception("unsupported decision result"))
      }
    }
  }

  def handle(transactionToClear: TransactionToClear): Either[Throwable, TransactionClearingResultEvent] = {
    val transaction = events.collect { case e: TransactionDecisionedEvent => e }.filter(_.transactionId == transactionToClear.transactionId)
    val isClearedAlready = events.collect { case e: TransactionClearingResultEvent => e }.exists(_.transactionId == transactionToClear.transactionId)

    if (isClearedAlready) {
      Left(new Exception(s"Transaction ${transactionToClear.transactionId} already cleared"))
    } else {
      transaction.headOption.map { t =>
        val clearingDecision = Clearing.clearTransaction(t, transactionToClear)
        Right(TransactionClearingResultEvent(t.accountId, t.transactionId, t.amount, clearingDecision, time.unixTimestampNow()))
      } match {
        case Some(x) => x
        case None => Left(new Exception(s"Transaction ${transactionToClear.transactionId} not found"))
      }
    }
  }

  def handle(payment: PaymentSubmittedEvent): Either[Throwable, TransactionPaymentAppliedEvent] = {
    if (events.collect { case p: TransactionPaymentAppliedEvent => p}.exists(_.paymentId == payment.paymentId)) {
      Left(new Exception(s"payment submitted id ${payment.paymentId} has been already processed"))
    } else {
      Right(TransactionPaymentAppliedEvent(payment.accountId, payment.paymentId, payment.amount, 1234))
    }
  }

  //TODO: potentially should validate returned paymentId already exists in EventStore
  def handle(payment: PaymentReturnedEvent): Either[Throwable, TransactionPaymentReturnedEvent] = {
    if (events.collect { case p: TransactionPaymentReturnedEvent => p}.exists(_.paymentId == payment.paymentId)) {
      Left(new Exception(s"payment returned id ${payment.paymentId} has been already processed"))
    } else {
      Right(TransactionPaymentReturnedEvent(payment.accountId, payment.paymentId, payment.amount, 1234))
    }
  }
}
