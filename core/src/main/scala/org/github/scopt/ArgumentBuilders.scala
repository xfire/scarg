package org.github.scopt

import collection.mutable.ListBuffer

trait ArgumentBuilders {
  self: ArgumentContainer =>

  /**
   * build a positional argument. the following forms are allowed:
   *
   *   + "required" |% "description" |> {action => action}
   *   ~ "optional" |% "description" |> {action => action}
   *
   *   +    -> required positional parameter
   *   *    -> optional positional parameter
   *
   *   |%   -> the description, optional
   *   |>   -> the action
   */
  class PositionalBuilder(name: String) {
    var description = ""
    var optional = false

    class Builder {
      def |%(desc: String) = {
        description = desc
        this
      }
      def |>(f: String => Unit) = {
        self.addArgument(PositionalArgument(name, description, optional, f))
      }
    }

    def unary_+ = new Builder
    def unary_~ = {
      optional = true
      new Builder
    }
  }

  implicit def toPositionalBuilder(name: String) = new PositionalBuilder(name)

  /**
   * build a option argument. the following forms are allowed:
   * 
   *   ! "-f" | "--foo" |^ "valueName" |* "defaultValue" |% "description" |> {action => action}
   *
   *   !   -> parameter name
   *
   *   |   -> more parameter names
   *   |^  -> name of the value, optional
   *   |*  -> default value, optional. if not give the parameter must be specified on the cmdl
   *          can be any object with a toString method.
   *   |%  -> the description, optional
   *   |>  -> the action
   */
  class OptionalBuilder(name: String) {
    val names = ListBuffer(name)
    var description = ""
    var valueName: Option[String] = None
    var default: Option[String] = None

    def unary_! = new Builder

    class Builder {
      def |(name: String) = {
        names += name
        this
      }

      def |%(desc: String) = {
        description = desc
        this
      }

      def |^(name: String) = {
        valueName = Some(name)
        this
      }

      def |*[T](value: T) = {
        default = Some(value.toString)
        this
      }

      def |>(f: String => Unit) = {
        self.addArgument(OptionArgument(names, valueName, description, default, f))
      }
    }

  }

  implicit def toOptionalBuilder(name: String) = new OptionalBuilder(name)
}

// vim: set ts=2 sw=2 et:
