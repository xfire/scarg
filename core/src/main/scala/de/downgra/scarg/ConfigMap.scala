package de.downgra

package object scarg {

  type ValueMap = Map[String, List[String]]

  abstract class ConfigMap(protected val vmap: ValueMap) extends Readers {
    def get[T : Reader](name: String): Option[T] = vmap get name map(_.head) map(implicitly[Reader[T]].read)
    def get[T : Reader](name: String, default: T): T = get[T](name) getOrElse (default)

    def getList[T : Reader](name: String): List[T] = getList[T](name, List())
    def getList[T : Reader](name: String, default: List[T]): List[T] = vmap get name map (_.map(implicitly[Reader[T]].read)) getOrElse(default)

    implicit def asDefaultWrapper[T : Reader](t: (String, T)) = new {
      def as[U <: T : Reader]: T = get[T](t._1) getOrElse(t._2)
    }

    implicit def asListDefaultWrapper[T : Reader](t: (String, List[T])) = new {
      def asList[U <: T : Reader]: List[T] = getList[T](t._1, t._2)
    }

    implicit def asListWrapper(name: String) = new {
      def as[T : Reader]: Option[T] = get[T](name)
      def asList[T : Reader]: List[T] = getList[T](name)
    }
  }

}

// vim: set ts=2 sw=2 et:
