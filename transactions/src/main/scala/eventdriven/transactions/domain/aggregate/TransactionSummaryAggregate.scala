package eventdriven.transactions.domain.aggregate

import eventdriven.core.domain.Aggregate
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.decisioning.Rules
import eventdriven.transactions.domain.event.payment.{PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.domain.event.transaction.{TransactionDecisioned, TransactionEvent, TransactionPaymentApplied, TransactionPaymentReturned}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.model.transaction.{PreDecisionedTransactionRequest, TransactionSummary}
import wvlet.log.LogSupport

object TransactionSummaryAggregate {
  def init(aggregateId: Int)
          (es: EventStore[TransactionEvent]): Either[Throwable, TransactionSummaryAggregate] = for {
    events <- es.get(aggregateId)
  } yield new TransactionSummaryAggregate(events)
}

class TransactionSummaryAggregate(events: List[TransactionEvent]) extends Aggregate[Int, TransactionSummary, TransactionEvent] with LogSupport {
  override def buildState: Option[TransactionSummary] = {
    if (events.isEmpty) None
    else {
      info(s"Applying following EventStore events: $events")
      val state = events
        .foldLeft(TransactionSummary(events.head.accountId, 0)) {
          (state, trx) => trx match {
            case TransactionDecisioned(_, _, _, amt, "Approved", _, _, _) => state.copy(balance = state.balance + amt)
            case TransactionPaymentApplied(_, _, amount, _) => state.copy(balance = state.balance - amount)
            case TransactionPaymentReturned(_, _, amount, _) => state.copy(balance = state.balance + amount)
            case _ => state
          }
        }
      info(s"New EventStore state: $state")
      Some(state)
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
        case Some(DecisionResult(Decision.Approved, version, _)) => Right(TransactionDecisioned(accountInfo.accountId, preAuth.cardNumber, preAuth.transactionId, preAuth.amount, "Approved", "", version, 1234))
        case Some(DecisionResult(Decision.Declined, version, Some(reason))) => Right(TransactionDecisioned(accountInfo.accountId, preAuth.cardNumber, preAuth.transactionId, preAuth.amount, "Declined", reason, version, 1234))
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
