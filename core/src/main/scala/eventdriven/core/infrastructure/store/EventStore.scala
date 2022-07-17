package eventdriven.core.infrastructure.store

case class EventStoreEnvelope[T](event: T, committed: Boolean)

trait EventStore[T] {
  /**
   * Get all events for given entity
   * @param entityId
   * @return
   */
  def get(entityId: Int): Either[Throwable, List[T]]

  /**
   * Append new event
   * @param event
   * @return
   */
  def append(event: T): Either[Throwable, Unit]
}
