package de.downgra.scarg

sealed trait Argument

/** separator line (only relevant for the help viewer) */
case class Separator(description: String = System.getProperty("line.separator")) extends Argument

/** a positional argument which comes after the option arguments and don't
 * start with dashed.
 *
 * e.g. `... input.txt output.txt` 
 *
 * @param name name of the argument
 * @param description the description of this argument
 * @param optional if `true` the argument is optional
 * @param repeated if `true` the argument can specified multiple times
 * @param the key which is used for the `ValueMap`
 * @author Rico Schiekel
 */
case class PositionalArgument(name: String,
                              description: String,
                              optional: Boolean,
                              repeated: Boolean,
                              key: String
                             ) extends Argument

/** a option argument startint with one or two dashes.
 *
 * e.g. `... -f --flag --key=value ...`
 *
 * @param names sequence of option names (`-f --flag ...`)
 * @param valueName the name of the value or `None` if it is a flag
 * @param description the description of this argument
 * @param default the default value or `None`
 * @param the key which is used for the `ValueMap`
 * @author Rico Schiekel
 */
case class OptionArgument(names: Seq[String],
                          valueName: Option[String],
                          description: String,
                          default: Option[String],
                          key: String
                         ) extends Argument
