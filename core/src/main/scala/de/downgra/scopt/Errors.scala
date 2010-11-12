package de.downgra.scarg

sealed trait ParseError

case class UnknownArgument(argument: String) extends ParseError
case class MissingOption(argument: String) extends ParseError
case class MissingPositional(argument: String) extends ParseError

// vim: set ts=2 sw=2 et:
