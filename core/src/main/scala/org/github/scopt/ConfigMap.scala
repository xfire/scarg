package org.github.scopt

trait ConfigMap {

  private var configurationMap: Map[String, String] = Map()

  def get[T : Reader](name: String): Option[T] = configurationMap get name map (implicitly[Reader[T]].read)
  def set(p: (String, String)) = configurationMap += p


  implicit object StringReader extends Reader[String] {
    def read(value: String): String = value
  }

  implicit object IntReader extends Reader[Int] {
    def read(value: String): Int = value.toInt
  }

  implicit object DoubleReader extends Reader[Double] {
    def read(value: String): Double = value.toDouble
  }
}

// vim: set ts=2 sw=2 et:
