package examples

import org.github.scopt.{ArgumentParser, ConfigMap}

/**
 * usage: SimpleExample [options] infile
 * 
 * options:
 *   -v, --verbose   
 *   -o OUT          
 *   infile          
 */
object SimpleExample {

  class Configuration extends ConfigMap {
    lazy val verbose = get[Boolean]("verbose") getOrElse false
    lazy val outfile = get[String]("outfile") getOrElse "-"
    lazy val infile = ("infile", "").as[String]
  }

  case class SimpleParser(config: ConfigMap) extends ArgumentParser {
    override val programName = Some("SimpleExample")
    
    ! "-v" | "--verbose"   |% "active verbose output"            |> config.set("verbose")
    ! "-o" |^ "OUT" |* "-" |% "output filename, default: stdout" |> { config.set("outfile", _) }
    + "infile"             |% "input filename"                   |> config.set("infile")
  }

  def main(args: Array[String]) {
    val c = new Configuration

    if(SimpleParser(c).parse(args)) {
      println("verbose: " + c.verbose)
      println("outfile: " + c.outfile)
      println(" infile: " + c.infile)
    }
  }

}

// vim: set ts=2 sw=2 et:
