package eventdriven.core.outboxpoller

trait OutboxPublisher[T] {
  def publish(event: T): Either[Throwable, Unit]
}
