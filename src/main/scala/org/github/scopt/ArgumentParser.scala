package org.github.scopt

import collection.mutable.{Buffer, ListBuffer, Stack => MStack}
import annotation.tailrec

trait ArgumentParser extends ArgumentContainer with ArgumentBuilders {

  override private[scopt] val arguments = new ListBuffer[Argument]

  private val NL = System.getProperty("line.separator")

  val INDENT = " " * 2
  val USAGE_HEADER = "usage: "
  val OPTIONS_AVAILABLE = "[options] "
  val OPTIONS_HEADER = "options:" + NL
  val UNKNOWN_ARGUMENT = "unknown argumen: "
  val MISSING_OPTION = "missing option: "
  val MISSING_POSITIONAL = "missing parameter: "

  val optionalMarker = ('[', ']')
  val optionDelimiters = ":="
  val programName: Option[String] = None
  val errorOnUnknownArgument = true
  
  override private[scopt] def addArgument(arg: Argument) = /* TODO: sanity check: double params, order, ... */ arguments += arg

  /** produce a list of argument descriptions */
  private def descriptions: Seq[String] = {

    def describeValue(value: Option[String]) = value map (" %s" format _) getOrElse ""

    /* "-f" "-f valueName" "-f, --foo" "-f valueName, --foo valueName" */
    def describeOption(o: OptionArgument) = (o.names.map (n =>
        (n + describeValue(o.valueName)).trim
      ) mkString ", ")

    val args = arguments.map ( _ match {
      case Separator(s)          => (s, "")
      case o: OptionArgument     => (describeOption(o), o.description)
      case p: PositionalArgument => (p.name, p.description)
    })

    val maxlen: Int = (args.map(_._1.length).foldLeft(0)((a,v) => if(a > v) a else v)) + 3

    // layout argument string and description
    args map(ap => ap._1 + (" " * (maxlen - ap._1.length)) + ap._2)
  }

  /** returns a list containing only all option arguments */
  def optionArguments: Seq[OptionArgument] = arguments.view flatMap {
    case o: OptionArgument => Some(o)
    case _                 => None
  }

  /** returns a list containing only all positional arguments */
  def positionalArguments: Seq[PositionalArgument] = arguments.view flatMap {
    case p: PositionalArgument => Some(p)
    case _                     => None
  }

  /** create the usage string */
  def usage: String = {
    def wrapOpt(s: String) = optionalMarker._1 + s + optionalMarker._2

    val prog = programName map(_ + " ") getOrElse ""
    val optionText = if (optionArguments.isEmpty) "" else OPTIONS_AVAILABLE
    val argumentList = positionalArguments map (p => if(p.optional) wrapOpt(p.name) else p.name) mkString(" ")
    val descText = OPTIONS_HEADER + INDENT + descriptions.mkString(NL + INDENT)

    USAGE_HEADER + prog + optionText + argumentList + (NL * 2) + descText + NL
  }

  def showUsage = Console.err.println(usage)

  def error(msg: String) = System.err.println("error: " + msg)

  private object Delimiter {
    def unapply(s: String): Option[(String, String)] =
      s.span(!optionDelimiters.contains(_)) match {
        case (a, b) if a.length > 0 && b.length > 0 => Some((a, b.tail))
        case _                      => None
      }
  }

  def parse(args: Seq[String]): Boolean = {
    val errors = parseRaw(args)
    if(errors.nonEmpty) {
      showUsage
      errors foreach (error _)
    }

    errors.isEmpty
  }

  def parseRaw(args: Seq[String]): Seq[String] = {
    val options = Map() ++ (optionArguments filter (_.valueName.isDefined) flatMap (o => o.names map ((_ -> o))))
    val flags = Map() ++ (optionArguments filter (_.valueName.isEmpty) flatMap (o => o.names map ((_ -> o))))
    val positionals = new MStack[PositionalArgument].pushAll(positionalArguments.reverse)

    var argumentsFound: Set[OptionArgument] = Set()
    var errors: List[String] = List()

    @tailrec def _parse(args: Seq[String]): Unit = args match {
      case o :: v :: t if(options contains o) => // -f value
        options get(o) map { a =>
          a.action(v)
          argumentsFound += a
        }
        _parse(t)
      case Delimiter(o, v) :: t if(options contains o) => // -f[:=]value
        options get(o) map { a =>
          a.action(v)
          argumentsFound += a
        }
        _parse(t)
      case f :: t if(flags contains f) => // -f
        flags get(f) map { a => 
          a.action("")
          argumentsFound += a
        }
        _parse(t)
      case p :: t if(positionals.nonEmpty && p(0) != '-') => // positionalParam
        val a = positionals.pop
        a.action(p)
        _parse(t)
      case o :: t => // unknown param
        errors = (UNKNOWN_ARGUMENT + o) :: errors
      case Nil =>
    }

    _parse(args)

    val notFoundOptions = optionArguments.toSet -- argumentsFound

    // set default values
    notFoundOptions filter (_.default.isDefined) foreach(o => o.action(o.default.get))


    // check if all necessary arguments are given
    errors = (notFoundOptions filter (_.default.isEmpty)).foldLeft(errors)((a,v) => (MISSING_OPTION + v.names(0)) :: a)
    errors = (positionals filter(_.optional == false)).foldLeft(errors)((a,v) => (MISSING_POSITIONAL + v.name) :: a)

    errors.reverse
  }
}
