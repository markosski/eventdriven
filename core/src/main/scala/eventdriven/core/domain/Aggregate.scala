package eventdriven.core.domain

trait Aggregate[I, T, E] {
  def buildState: Option[T]
}

object Aggregate {

}
