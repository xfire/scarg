package examples

import de.downgra.scarg.{ArgumentParser, ConfigMap}

/**
 * usage: SimpleExample [options] infile
 *
 * options:
 *   -v, --verbose
 *   -o OUT
 *   infile
 */
object AlternateSyntaxExample {

  class Configuration extends ConfigMap {
    lazy val verbose = get[Boolean]("verbose") getOrElse false
    lazy val outfile = get[String]("outfile") getOrElse "-"
    lazy val infile = ("infile", "").as[String]
  }

  case class SimpleParser(config: ConfigMap) extends ArgumentParser {
    override val programName = Some("AlternateSyntaxExample")

    newOptional("-v").name("--verbose").description("active verbose output").
                      action(config.set("verbose"))
    newOptional("-o").valueName("OUT").description("output filename, default: stdout").
                      action(config.set("outfile", _))

    newSeparator("-", 50)

    newPositional("infile").required.description("input filename").
                            action(config.set("infile"))
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
