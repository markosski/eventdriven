package eventdriven.core.models

trait Aggregate[I, T, E] {
  val getState: Option[T]
}
