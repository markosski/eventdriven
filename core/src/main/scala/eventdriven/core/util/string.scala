package eventdriven.core.util

import java.util.UUID

object string {
  def getUUID(): String = UUID.randomUUID().toString
}
