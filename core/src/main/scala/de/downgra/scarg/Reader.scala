package de.downgra.scarg

/** interface for the conversion type classes */
trait Reader[T] {
  def read(value: String): T
}

/** some default implementations which can be mixed in */
trait Readers {
  implicit object StringReader extends Reader[String] {
    def read(value: String): String = value
  }

  implicit object IntReader extends Reader[Int] {
    def read(value: String): Int = try {
      value.toInt
    } catch {
      case e => throw new IllegalArgumentException("Expect a string I can interpret as an integer", e)
    }
  }

  implicit object DoubleReader extends Reader[Double] {
    def read(value: String): Double = try {
      value.toDouble
    } catch {
      case e => throw new IllegalArgumentException("Expect a string I can interpret as an double", e)
    }
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

/** the same default implementations which can be imported */
object Readers extends Readers

// vim: set ts=2 sw=2 et:
