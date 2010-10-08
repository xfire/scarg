package de.downgra.scarg

trait Argument

case class Separator(description: String = System.getProperty("line.separator")) extends Argument

case class PositionalArgument(name: String,
                              description: String,
                              optional: Boolean,
                              key: String
                             ) extends Argument

case class OptionArgument(names: Seq[String],
                          valueName: Option[String],
                          description: String,
                          default: Option[String],
                          key: String
                         ) extends Argument
