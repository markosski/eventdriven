package eventdriven.core.infrastructure.store

trait Cache[T] {
  def exists(key: String): Boolean
  def get(key: String): Option[T]
  def put(key: String, value: T): Unit
}
