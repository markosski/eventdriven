package eventdriven.transactions.domain

package object event {
  case class Event[T](payload: T, eventId: String, eventName: String, eventTimestamp: Long)
}
