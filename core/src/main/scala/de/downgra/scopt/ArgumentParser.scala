package de.downgra.scarg

import collection.mutable.{Buffer, ListBuffer, Stack => MStack}
import annotation.tailrec

class DoubleArgumentException(val message: String) extends RuntimeException(message)
class BadArgumentOrderException(val message: String) extends RuntimeException(message)


abstract class ArgumentParser[T](configFactory: ValueMap => T) extends ArgumentContainer with ArgumentBuilders {

  /** the parse result, either left with a list of error messages, or right with the value created by `configFactory` */
  type ParseResult = Either[List[String], T]

  override private[scarg] val arguments = new ListBuffer[Argument]

  val NL = System.getProperty("line.separator")
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

  /** if true, show usage and error message after parsing arguments */
  val showErrors = true

  /** the default value for flags (first if flag is given, else second) */
  val flagDefaults = ("true", "false")
  
  @throws(classOf[DoubleArgumentException])
  @throws(classOf[BadArgumentOrderException])
  override private[scarg] def addArgument(arg: Argument) = {
    arg match {
      case PositionalArgument(name,_,optional,_) =>
        // check double entries
        if(positionalArguments exists (_.name == name))
          throw new DoubleArgumentException("Positional argument %s already exists." format (name))
        // no required argument after an optional
        if(!optional && positionalArguments.exists(_.optional == true))
          throw new BadArgumentOrderException(
            "Required positional arguments are not allowed after optional positional arguments (%s)" format (name))
      case OptionArgument(names,valueName,default,_,_) =>
        // check double entries
        if(optionArguments exists (a => (a.names intersect names).nonEmpty))
          throw new DoubleArgumentException("Positional argument %s already exists." format (names(0)))
      case _ =>
    }
    arguments += arg
  }

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

  def parse(args: Seq[String]): ParseResult = {
    val options = Map() ++ (optionArguments filter (_.valueName.isDefined) flatMap (o => o.names map ((_ -> o))))
    val flags = Map() ++ (optionArguments filter (_.valueName.isEmpty) flatMap (o => o.names map ((_ -> o))))
    val positionals = new MStack[PositionalArgument].pushAll(positionalArguments.reverse)

    var argumentsFound: Set[OptionArgument] = Set()
    var errors: List[String] = List()
    var result: ValueMap = Map()

    @tailrec def _parse(args: Seq[String]): Unit = args.toList match {
      // ___ -f value
      case o :: v :: t if(options.contains(o) && v(0) != '-') =>
        options get(o) map { a =>
          result += (a.key -> (v :: result.getOrElse(a.key, Nil)))
          argumentsFound += a
        }
        _parse(t)
      // ___ -f[:=]value
      case Delimiter(o, v) :: t if(options contains o) =>
        options get(o) map { a =>
          result += (a.key -> (v :: result.getOrElse(a.key, Nil)))
          argumentsFound += a
        }
        _parse(t)
      // ___ -f
      case f :: t if(flags contains f) =>
        flags get(f) map { a => 
          // flags are booleans per default
          result += (a.key -> (flagDefaults._1 :: result.getOrElse(a.key, Nil)))
          argumentsFound += a
        }
        _parse(t)
      // ___ positionalParam
      case p :: t if(positionals.nonEmpty && p(0) != '-') =>
        val a = positionals.pop
        result += (a.key -> (p :: result.getOrElse(a.key, Nil)))
        _parse(t)
      // ___ unknown param
      case o :: t =>
        if(errorOnUnknownArgument) {
          errors = (UNKNOWN_ARGUMENT + o) :: errors
        }
        _parse(t)
      case Nil =>
    }

    _parse(args)

    val notFoundOptions = optionArguments.toSet -- argumentsFound

    // need to set default for not given flags with value "false"
    notFoundOptions filter (_.valueName.isEmpty) foreach { a =>
      result += (a.key -> (flagDefaults._2 :: result.getOrElse(a.key, Nil)))
    }

    // set default values for all option arguments
    notFoundOptions filter (o => o.default.isDefined && o.valueName.nonEmpty) foreach { a => 
      result += (a.key -> (a.default.get :: result.getOrElse(a.key, Nil)))
    }

    // check if all necessary arguments are given
    errors = (notFoundOptions filter (o => o.default.isEmpty && o.valueName.isDefined) // only options which are not flags
             ).foldLeft(errors)((a,v) => (MISSING_OPTION + v.names(0)) :: a)
    errors = (positionals filter(_.optional == false)).foldLeft(errors)((a,v) => (MISSING_POSITIONAL + v.name) :: a)

    if(errors.nonEmpty) {
      if(showErrors) {
        showUsage
        errors.reverse foreach (error _)
      }
      Left(errors.reverse)
    } else Right(configFactory(result))
  }
}
