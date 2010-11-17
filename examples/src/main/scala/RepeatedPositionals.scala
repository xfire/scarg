package examples

import de.downgra.scarg.{ArgumentParser, ConfigMap, ValueMap, DefaultHelpViewer}

/**
 * usage: RepeatedPositionals [options] [infiles]
 * 
 * options:
 *   -v, --verbose   active verbose output
 *   --------------------------------------------------
 *   infiles         multiple (0..n) input filenames
 * 
 */
object RepeatedPositionals {

  class Configuration(m: ValueMap) extends ConfigMap(m) {
    val verbose = ("verbose", false).as[Boolean]
    val infiles = ("infiles").asList[String]
  }

  case class RepeatedPositionalParser()
        extends ArgumentParser(new Configuration(_))
           with DefaultHelpViewer {
    override val programName = Some("RepeatedPositionals")

    ! "-v" | "--verbose"   |% "active verbose output"           |> "verbose"
    ("-" >>> 50)
    ~ "infiles"            |% "multiple (0..n) input filenames" |*> 'infiles
  }

  def main(args: Array[String]) {
    RepeatedPositionalParser().parse(args) match {
      case Right(c) =>
        println("verbose: " + c.verbose)
        println("infiles: " + c.infiles)
      case Left(xs) =>
    }
  }

}

// vim: set ts=2 sw=2 et:
