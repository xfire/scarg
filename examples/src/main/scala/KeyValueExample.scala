package examples

import de.downgra.scarg.{ArgumentParser, ConfigMap, Reader}

/*
 * usage: KeyValueExample [options] 
 * 
 * options:
 *   -k Key=Value   a key=value pair
 *
 */
object KeyValueExample {

  case class KeyValuePair(key: String, value: String)

  class KeyValueConfig extends ConfigMap {
    // type class to read an KeyValuePair
    implicit object KeyValueReader extends Reader[KeyValuePair] {
      def read(value: String): KeyValuePair = value.indexOf('=') match {
          case n: Int if n >= 0 => 
            val p = value.splitAt(n)
            KeyValuePair(p._1, p._2)
          case _ => throw new IllegalArgumentException("Expected a key=value pair")
        }
    }

    lazy val pair = ("pair", KeyValuePair("", "")).as[KeyValuePair]
  }

  case class KeyValueParser(config: ConfigMap) extends ArgumentParser {
    override val programName = Some("KeyValueExample")
    
    ! "-k" |^ "Key=Value" |% "a key=value pair" |> config.set("pair")
  }

  def main(args: Array[String]) {
    val c = new KeyValueConfig

    if(KeyValueParser(c).parse(args)) {
      println("got key=%s and value=%s" format (c.pair.key, c.pair.value))
    }
  }

}

// vim: set ts=2 sw=2 et:
