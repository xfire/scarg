package de.downgra.scarg

import collection.mutable.Buffer

/** Abstract argument container.
 *
 * @author Rico Schiekel
 */
trait ArgumentContainer {

  /** `Buffer` which holds all arguments */
  protected[scarg] val arguments: Buffer[Argument]

  /** add a single argument to the argument list */
  protected[scarg] def addArgument(arg: Argument): Unit

  /** returns a list containing only all option arguments */
  protected[scarg] def optionArguments: Seq[OptionArgument] = arguments.view flatMap {
    case o: OptionArgument => Some(o)
    case _                 => None
  }

  /** returns a list containing only all positional arguments */
  protected[scarg] def positionalArguments: Seq[PositionalArgument] = arguments.view flatMap {
    case p: PositionalArgument => Some(p)
    case _                     => None
  }

}
