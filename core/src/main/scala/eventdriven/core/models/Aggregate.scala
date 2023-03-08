package eventdriven.core.models

trait Aggregate[I, T, E] {
  def buildState: Option[T]
}

object Aggregate {

}
