package org.github.scopt

trait Argument

case class Separator(description: String = System.getProperty("line.separator")) extends Argument

case class PositionalArgument(name: String,
                              description: String,
                              optional: Boolean,
                              action: String => Unit
                             ) extends Argument

case class OptionArgument(names: Seq[String],
                          valueName: Option[String],
                          description: String,
                          default: Option[String],
                          action: String => Unit
                         ) extends Argument

