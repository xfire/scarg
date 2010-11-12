package de.downgra.scarg

import collection.mutable.Buffer

trait ArgumentContainer {

  protected[scarg] val arguments: Buffer[Argument]

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
