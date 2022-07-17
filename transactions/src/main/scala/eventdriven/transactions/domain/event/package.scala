package eventdriven.transactions.domain

package object event {
  case class Event[T](payload: T, eventTimestamp: Long)
}
