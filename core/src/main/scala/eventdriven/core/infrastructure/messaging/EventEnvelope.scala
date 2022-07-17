package eventdriven.core.infrastructure.messaging

case class EventEnvelope[T](eventType: String, version: String, eventTimeInMillis: Long, payload: T)
