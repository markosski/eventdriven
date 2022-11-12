package eventdriven.core.outboxpoller

trait OutboxEventStore[T] {
  def getUnpublished(limit: Int): Either[Throwable, List[T]]
  def deleteEvent(event: T): Either[Throwable, Unit]
}
