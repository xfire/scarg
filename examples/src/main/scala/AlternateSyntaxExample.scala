package examples

import de.downgra.scarg.{ArgumentParser, ConfigMap, ValueMap, DefaultHelpViewer}

/**
 * usage: AlternateSyntaxExample [options] infile
 * 
 * options:
 *   -v, --verbose   active verbose output
 *   -o OUT          output filename, default: stdout
 *   --------------------------------------------------
 *   infile          input filename
 * 
 */
object AlternateSyntaxExample {

  class Configuration(m: ValueMap) extends ConfigMap(m) {
    val verbose = get[Boolean]("verbose") getOrElse false
    val outfile = get[String]("outfile") getOrElse "-"
    val infile = ("infile", "").as[String]
  }

  case class SimpleParser() extends ArgumentParser(new Configuration(_))
                               with DefaultHelpViewer {
    override val programName = Some("AlternateSyntaxExample")

    optional("-v").name("--verbose").description("active verbose output").
                      key("verbose")
    optional("-o").valueName("OUT").description("output filename, default: stdout").
                      key("outfile")

    separator("-", 50)

    positional("infile").required description("input filename") key("infile")
  }

  def main(args: Array[String]) {
    SimpleParser().parse(args) match {
      case Right(c) =>
        println("verbose: " + c.verbose)
        println("outfile: " + c.outfile)
        println(" infile: " + c.infile)
      case Left(xs) =>
    }
  }

}

// vim: set ts=2 sw=2 et:
