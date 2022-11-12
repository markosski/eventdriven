package eventdriven.core.infrastructure.messaging

case class EventEnvelope[T](id: String, eventType: String, eventTimeInMillis: Long, payload: T)

