package eventdriven.core.infrastructure.messaging

trait EventDispatcher[E] {
  def publish(id: String, event: E, topic: String): Either[Throwable, Unit]
}
