package eventdriven.core.infrastructure.messaging

trait EventListener[E] {
  def take: Option[E]
}
