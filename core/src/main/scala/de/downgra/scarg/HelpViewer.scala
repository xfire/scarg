package de.downgra.scarg

/** Interface for help message generation.
 *
 * @author Rico Schiekel
 */
trait HelpViewer {
  def usage: String
  def showUsage: Unit
  def showErrors(errors: List[ParseError]): Unit
}

/** Simple `HelpViewer` implementation, which generates no output.
 *
 * @author Rico Schiekel
 */
trait SilentHelpViewer {
  def usage: String = ""
  def showUsage: Unit = {}
  def showErrors(errors: List[ParseError]): Unit = {}
}

/** Default `HelpViewer` implementation, which generates scarg's default help messages.
 * can only be mixed into `ArgumentContainer`'s.
 *
 * most of the values like `NL`, `INDENT`, `USAGE_HEADER`, ... can be overriden to 
 * personalize or i18n the output.
 *
 * @author Rico Schiekel
 */
trait DefaultHelpViewer extends HelpViewer {
  this: ArgumentContainer =>

  /** new line separator */
  val NL = System.getProperty("line.separator")
  /** default indent */
  val INDENT = " " * 2
  /** usage header text */
  val USAGE_HEADER = "usage: "
  /** option header text */
  val OPTIONS_AVAILABLE = "[options] "
  /** option list header text */
  val OPTIONS_HEADER = "options:" + NL
  /** error text: unknown argument */
  val UNKNOWN_ARGUMENT = "unknown argumen: "
  /** error text: missing option */
  val MISSING_OPTION = "missing option: "
  /** error text: missing parameter */
  val MISSING_POSITIONAL = "missing parameter: "

  /** optional marker text (pair of two char's) */
  val optionalMarker = ('[', ']')
  /** program name */
  val programName: Option[String] = None

  /** the default output to use (e.g. stderr) */
  def output(s: String) = Console.err.println(s)

  /** produce a list of argument descriptions */
  private def descriptions: Seq[String] = {

    def describeValue(value: Option[String]) = value map (" %s" format _) getOrElse ""

    /* "-f" "-f valueName" "-f, --foo" "-f valueName, --foo valueName" */
    def describeOption(o: OptionArgument) = (o.names.map (n =>
        (n + describeValue(o.valueName)).trim
      ) mkString ", ")

    val args = arguments.map ( _ match {
      case Separator(s)          => Left(s)
      case o: OptionArgument     => Right(describeOption(o), o.description)
      case p: PositionalArgument => Right(p.name, p.description)
    })

    val maxlen: Int = (args.filter(_.isRight).map(_.right.get._1.length).foldLeft(0)((a,v) => if(a > v) a else v)) + 3

    // layout argument string and description
    args map( _ match {
      case Left(s)                     => s
      case Right((n, d)) if(d.isEmpty) => n
      case Right((n, d))               => n + (" " * (maxlen - n.length)) + d
      case _                           => throw new RuntimeException("we failed badly... escape the ship")
    })
  }


  /** create and return the usage string */
  def usage: String = {
    def wrapOpt(s: String) = optionalMarker._1 + s + optionalMarker._2

    val prog = programName map(_ + " ") getOrElse ""
    val optionText = if (optionArguments.isEmpty) "" else OPTIONS_AVAILABLE
    val argumentList = positionalArguments map (p => if(p.optional) wrapOpt(p.name) else p.name) mkString(" ")
    val descText = OPTIONS_HEADER + INDENT + descriptions.mkString(NL + INDENT)

    USAGE_HEADER + prog + optionText + argumentList + (NL * 2) + descText + NL
  }

  /** show usage on the screen */
  def showUsage: Unit = output(usage)

  /** show given list of parse errors on the screen */
  def showErrors(errors: List[ParseError]) = errors.map(_ match {
    case UnknownArgument(a)   => UNKNOWN_ARGUMENT + a
    case MissingOption(o)     => MISSING_OPTION + o
    case MissingPositional(p) => MISSING_POSITIONAL + p
  }).foreach(output _)
}

// vim: set ts=2 sw=2 et:
