package eventdriven.transactions.usecase.aggregate

import eventdriven.core.domain.Aggregate
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.event.payment.{PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.domain.event.transaction.{TransactionDecisioned, TransactionEvent, TransactionPaymentApplied, TransactionPaymentReturned}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.model.transaction.{PreDecisionedTransactionRequest, TransactionBalance}
import eventdriven.transactions.usecase.decisioning.Rules
import eventdriven.transactions.usecase.projection.TransactionBalanceProjection
import wvlet.log.LogSupport

object TransactionDecisionAggregate {
  def init(aggregateId: Int)
          (es: EventStore[TransactionEvent]): Either[Throwable, TransactionDecisionAggregate] = for {
    events <- es.get(aggregateId)
  } yield new TransactionDecisionAggregate(events)
}

class TransactionDecisionAggregate(events: List[TransactionEvent]) extends Aggregate[Int, TransactionBalance, TransactionEvent] with LogSupport {
  val balanceProjection = new TransactionBalanceProjection(events)
  override def buildState: Option[TransactionBalance] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      balanceProjection.get
    }
  }

  def handle(preAuth: PreDecisionedTransactionRequest, accountInfo: AccountInfo): Either[Throwable, TransactionDecisioned] = {
    if (events.collect { case t: TransactionDecisioned => t}.exists(_.transactionId == preAuth.transactionId)) {
      Left(new Exception(s"transaction id ${preAuth.transactionId} has been already processed"))
    } else {
      val decision = for {
        trxSummary <- buildState
      } yield Rules.current.run(preAuth, trxSummary, accountInfo)

      decision match {
        case None => Left(new Exception("could not decision, missing account data"))
        case Some(DecisionResult(Decision.Approved, version, _)) => Right(
          TransactionDecisioned(
            accountInfo.accountId,
            preAuth.cardNumber,
            preAuth.transactionId,
            preAuth.amount,
            "Approved",
            "",
            version,
            1234))
        case Some(DecisionResult(Decision.Declined, version, Some(reason))) =>
          Right(
            TransactionDecisioned(
              accountInfo.accountId,
              preAuth.cardNumber,
              preAuth.transactionId,
              preAuth.amount,
              "Declined",
              reason,
              version,
              1234))
        case _ => Left(new Exception("unsupported decision result"))
      }
    }
  }

  def handle(payment: PaymentSubmitted): Either[Throwable, TransactionPaymentApplied] = {
    if (events.collect { case p: TransactionPaymentApplied => p}.exists(_.paymentId == payment.paymentId)) {
      Left(new Exception(s"payment submitted id ${payment.paymentId} has been already processed"))
    } else {
      Right(TransactionPaymentApplied(payment.accountId, payment.paymentId, payment.amount, 1234))
    }
  }

  //TODO: potentially should validate returned paymentId already exists in EventStore
  def handle(payment: PaymentReturned): Either[Throwable, TransactionPaymentReturned] = {
    if (events.collect { case p: TransactionPaymentReturned => p}.exists(_.paymentId == payment.paymentId)) {
      Left(new Exception(s"payment returned id ${payment.paymentId} has been already processed"))
    } else {
      Right(TransactionPaymentReturned(payment.accountId, payment.paymentId, payment.amount, 1234))
    }
  }
}