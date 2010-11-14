package de.downgra.scarg

/** Error's which may be returned by the `ArgumentParser` */
sealed trait ParseError

/** Unknown Argument found Error */
case class UnknownArgument(argument: String) extends ParseError
/** Missing Option Error */
case class MissingOption(argument: String) extends ParseError
/** Missing Positional Error */
case class MissingPositional(argument: String) extends ParseError

// vim: set ts=2 sw=2 et:
