package eventdriven.core.domain

trait Projection[T, S] {
  def get: Option[S]
}