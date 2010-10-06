package de.downgra.scarg

trait Reader[T] {
  def read(value: String): T
}

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

object Reders extends Readers

// vim: set ts=2 sw=2 et:
