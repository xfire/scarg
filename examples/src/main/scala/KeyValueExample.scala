package examples

import de.downgra.scarg.{ArgumentParser, ConfigMap, Reader, ValueMap, DefaultHelpViewer}

/*
 * usage: KeyValueExample [options] 
 * 
 * options:
 *   -k Key=Value   a key=value pair
 *
 */
object KeyValueExample {

  case class KeyValuePair(key: String, value: String)

  class KeyValueConfig(m: ValueMap) extends ConfigMap(m) {
    // type class to read an KeyValuePair
    implicit object KeyValueReader extends Reader[KeyValuePair] {
      def read(value: String): KeyValuePair = value.indexOf('=') match {
          case n: Int if n >= 0 => 
            val p = value.splitAt(n)
            KeyValuePair(p._1, p._2)
          case _ => throw new IllegalArgumentException("Expected a key=value pair")
        }
    }

    val pair = ("pair", KeyValuePair("", "")).as[KeyValuePair]
  }

  case class KeyValueParser() extends ArgumentParser(new KeyValueConfig(_))
                                 with DefaultHelpViewer {
    override val programName = Some("KeyValueExample")
    
    ! "-k" |^ "Key=Value" |% "a key=value pair" |> 'pair
  }

  def main(args: Array[String]) {
    KeyValueParser().parse(args) match {
      case Right(c) =>
        println("got key=%s and value=%s" format (c.pair.key, c.pair.value))
      case Left(xs) =>
    }
  }

}

// vim: set ts=2 sw=2 et:
