package de.downgra.scarg

import collection.mutable.{Buffer, ListBuffer, Stack => MStack}
import annotation.tailrec

/** Exception if an argument is specified twice */
class DoubleArgumentException(val message: String) extends RuntimeException(message)
/** Exception if the order of the specified arguments is wrong */
class BadArgumentOrderException(val message: String) extends RuntimeException(message)

/** The Argument Parser implementation. It's indented to be subclassed and using the 
 * argument builders to add the argument specification. The constructor expects a
 * factory which convert's a `ValueMap` to a user defined type `T`.
 *
 * @param configFactory factory to create a user defined configuration map from a `ValueMap`
 * @tparam T user defined configuration map type
 * @author Rico Schiekel
 */
abstract class ArgumentParser[T](configFactory: ValueMap => T) extends ArgumentContainer
                                                                  with HelpViewer
                                                                  with ArgumentBuilders {

  /** the parse result, either left with a list of error messages, or right with the value created by `configFactory` */
  type ParseResult = Either[List[ParseError], T]

  override protected[scarg] val arguments = new ListBuffer[Argument]

  /** all valid option delimiters (e.g. --foo=bar, --bar:foo, ...) */
  val optionDelimiters = ":="

  /** handle unknown arguments as error? */
  val errorOnUnknownArgument = true

  /** if true, show usage and error message after parsing arguments */
  val showErrors = true

  /** the default value for flags (first if flag is given, else second) */
  val flagDefaults = ("true", "false")
  
  @throws(classOf[DoubleArgumentException])
  @throws(classOf[BadArgumentOrderException])
  override protected[scarg] def addArgument(arg: Argument) = {
    arg match {
      case PositionalArgument(name,_,optional,_,_) =>
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
          throw new DoubleArgumentException("Positional argument %s already exists." format (names))
        // no more option arguments allowed after positionals
        if(positionalArguments nonEmpty)
          throw new BadArgumentOrderException("After repeated positional arguments are no more arguments allowed (%s)" format (names))
      case _ =>
    }
    // no more arguments allowed after repeated positional
    if(positionalArguments exists (_.repeated))
      throw new BadArgumentOrderException("After a repeated positional argument are no more arguments allowed")
    arguments += arg
  }

  /** helper, to parse option delimiters */
  private object Delimiter {
    def unapply(s: String): Option[(String, String)] =
      s.span(!optionDelimiters.contains(_)) match {
        case (a, b) if a.length > 0 && b.length > 0 => Some((a, b.tail))
        case _                      => None
      }
  }

  /** parse a list of argument strings and return a `ParseResult`, which is a list of error messages or an instance
   * created with the `configFactory` factory.
   *
   * @param args list of argument strings
   * @return a `ParseResult`
   */
  def parse(args: Seq[String]): ParseResult = {
    val options = Map() ++ (optionArguments.filter(_.valueName.isDefined)
                                           .flatMap (o => o.names map ((_ -> o))))
    val flags = Map() ++ (optionArguments.filter(_.valueName.isEmpty)
                                         .flatMap (o => o.names map ((_ -> o))))
    val positionals = new MStack[PositionalArgument].pushAll(positionalArguments.reverse)

    var repeatedPositionalsFound: Set[PositionalArgument] = Set()
    var optionalsFound: Set[OptionArgument] = Set()
    var errors: List[ParseError] = List()
    var result: ValueMap = Map()

    /** found new optional */
    def newOptional(arg: Option[OptionArgument], value: String) = arg map { a =>
      result += (a.key -> (value :: result.getOrElse(a.key, Nil)))
      optionalsFound += a
    }
  
    @tailrec def _parse(args: Seq[String]): Unit = args.toList match {
      // ___ -f value
      case o :: v :: t if(options.contains(o) && v(0) != '-') =>
        newOptional(options.get(o), v)
        _parse(t)
      // ___ -f[:=]value
      case Delimiter(o, v) :: t if(options contains o) =>
        newOptional(options.get(o), v)
        _parse(t)
      // ___ -f
      case f :: t if(flags contains f) =>
        // flags are booleans per default
        newOptional(flags.get(f), flagDefaults._1)
        _parse(t)
      // ___ positionalParam
      case p :: t if(positionals.nonEmpty && p(0) != '-') =>
        val a = positionals.head
        // remember repeated positional arguments for later check
        if(!a.repeated) positionals.pop else repeatedPositionalsFound += a
        result += (a.key -> (p :: result.getOrElse(a.key, Nil)))
        _parse(t)
      // ___ unknown param
      case o :: t =>
        if(errorOnUnknownArgument) {
          errors = UnknownArgument(o) :: errors
        }
        _parse(t)
      case Nil =>
    }

    _parse(args)

    val missingOptionals = optionArguments.toSet -- optionalsFound
    val missingPositionals = positionals.toSet -- repeatedPositionalsFound // remove found repeated arguments

    result = (defValsOptionals(missingOptionals)_ andThen
              defValsPositionals(missingPositionals) _)(result)

    errors = (checkOptionals(missingOptionals)_ andThen
              checkPositionals(missingPositionals)_ )(errors)

    if(errors.nonEmpty) {
      if(showErrors) {
        showUsage
        showErrors(errors.reverse)
      }
      Left(errors.reverse)
    } else Right(configFactory(result mapValues (_.reverse)))
  }

  /** set default values for option arguments */
  private def defValsOptionals(missingOptionals: Set[OptionArgument])(foundValues: ValueMap): ValueMap = {
    def append(a: OptionArgument, v: String) = (a.key -> (v :: foundValues.getOrElse(a.key, Nil)))

    // need to set default for not given flags with value "false"
    val a = missingOptionals.filter(_.valueName.isEmpty)
                            .map( a => append(a, flagDefaults._2) )
                            .toMap

    // set default values for all option arguments
    val b = missingOptionals.filter(o => o.default.isDefined && o.valueName.nonEmpty)
                            .map( a => append(a, a.default.get) )
                            .toMap
    foundValues ++ a ++ b
  }

  /** set default values for optional repeated positional values */
  private def defValsPositionals(missingPositionals: Set[PositionalArgument])(foundValues: ValueMap): ValueMap = {
    val mp = missingPositionals.filter(p => p.optional && p.repeated)
                               .map( a => (a.key -> Nil) )
                               .toMap
    foundValues ++ mp
  }

  /** check if all necessary arguments are given */
  private def checkOptionals(missingOptionals: Set[OptionArgument])(errors: List[ParseError]): List[ParseError] = {
    missingOptionals.filter(o => o.default.isEmpty && o.valueName.isDefined) // only options which are not flags
                    .foldLeft(errors)((a,v) => MissingPositional(v.names(0)) :: a)
  }

  /** check missing positional arguments without the optional or found repeated */
  private def checkPositionals(missingPositionals: Set[PositionalArgument])(errors: List[ParseError]): List[ParseError] = {
    missingPositionals.filter(p => p.optional == false)
                      .foldLeft(errors)((a,v) => MissingPositional(v.name) :: a)
  }
}
