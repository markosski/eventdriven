package eventdriven.core.models

trait Projection[T, S] {
  def get: Option[S]
}