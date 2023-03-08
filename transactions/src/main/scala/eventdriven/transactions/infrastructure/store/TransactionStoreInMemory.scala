package eventdriven.transactions.infrastructure.store

import eventdriven.transactions.domain.events.TransactionEvent
import eventdriven.core.infrastructure.store.EventStore

import scala.collection.mutable
import scala.util.Try


class TransactionStoreInMemory(data: mutable.ListBuffer[TransactionEvent]) extends EventStore[TransactionEvent] {
  override def get(entityId: Int): Either[Throwable, List[TransactionEvent]] = {
    Try(data.filter(_.accountId == entityId).toList).toEither
  }

  override def append(transaction: TransactionEvent): Either[Throwable, Unit] = {
    Right(data.append(transaction))
  }
}