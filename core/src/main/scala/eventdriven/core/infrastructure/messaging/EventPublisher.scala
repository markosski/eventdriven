package eventdriven.core.infrastructure.messaging

trait EventPublisher[E] {
  def publish(id: String, event: E, topic: String): Either[Throwable, Unit]
}
