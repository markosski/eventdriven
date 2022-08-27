package eventdriven.core.infrastructure.store

import scala.collection.mutable

class CacheInMem[T] extends Cache[T] {
  val mmap = new mutable.HashMap[String, T]()

  override def exists(key: String): Boolean = {
    mmap.get(key).fold(false)(_ => true)
  }

  override def get(key: String): Option[T] = {
    mmap.get(key)
  }

  override def put(key: String, value: T) = {
    mmap.put(key, value)
  }
}
