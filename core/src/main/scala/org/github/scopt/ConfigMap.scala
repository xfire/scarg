package org.github.scopt

trait ConfigMap {

  private var configurationMap: Map[String, String] = Map()

  def get[T : Reader](name: String): Option[T] = configurationMap get name map (implicitly[Reader[T]].read)
  def set(p: (String, String)) = configurationMap += p
  def set(name: String)(value: String) = configurationMap += (name -> value)


  implicit object StringReader extends Reader[String] {
    def read(value: String): String = value
  }

  implicit object IntReader extends Reader[Int] {
    def read(value: String): Int = value.toInt
  }

  implicit object DoubleReader extends Reader[Double] {
    def read(value: String): Double = value.toDouble
  }

  implicit object BooleanReader extends Reader[Boolean] {
    def read(value: String): Boolean = value.toLowerCase match {
      case "true"  | "yes" | "1" => true
      case "false" | "no"  | "0" => false
      case _ =>
        throw new IllegalArgumentException("Expected a string I can interpret as a boolean")
    }
  }
}

// vim: set ts=2 sw=2 et:
