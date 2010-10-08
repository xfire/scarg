package de.downgra.scarg

import collection.mutable.ListBuffer

trait ArgumentBuilders {
  self: ArgumentContainer =>

  /**
   * build a positional argument. the following forms are allowed:
   *
   *   + "required" |% "description" |> 'key
   *   ~ "optional" |% "description" |> 'key
   *
   *   +    -> required positional parameter
   *   ~    -> optional positional parameter
   *
   *   |%   -> the description, optional
   *   |>   -> the key
   */
  class PositionalBuilder(_name: String) {
    var _description = ""
    var _optional = false

    class Builder {
      def |%(desc: String) = {
        _description = desc
        this
      }
      def |>(key: String) {
        self.addArgument(PositionalArgument(_name, _description, _optional, key))
      }
      def |>(key: Symbol): Unit = |>(key.name)

      def description = |% _
      def key(name: String) = |>(name)
      def key(name: Symbol) = |>(name)
    }

    def unary_+ = required
    def unary_~ = optional

    def required = new Builder
    def optional = {
      _optional = true
      new Builder
    }
  }

  def newPositional(name: String) = new PositionalBuilder(name)

  implicit def toPositionalBuilder(name: String) = new PositionalBuilder(name)

  /**
   * build a option argument. the following forms are allowed:
   * 
   *   ! "-f" | "--foo" |^ "valueName" |* "defaultValue" |% "description" |> 'key
   *
   *   !   -> parameter name
   *
   *   |   -> more parameter names
   *   |^  -> name of the value, optional
   *   |*  -> default value, optional. if not give the parameter must be specified on the cmdl
   *          can be any object with a toString method.
   *   |%  -> the description, optional
   *   |>  -> the key
   */
  class OptionalBuilder(_name: String) {
    val _names = ListBuffer(_name)
    var _description = ""
    var _valueName: Option[String] = None
    var _default: Option[String] = None

    def unary_! = new Builder

    class Builder {
      def |(name: String) = {
        _names += name
        this
      }

      def |%(desc: String) = {
        _description = desc
        this
      }

      def |^(name: String) = {
        _valueName = Some(name)
        this
      }

      def |*[T](value: T) = {
        _default = Some(value.toString)
        this
      }

      def |>(key: String) {
        self.addArgument(OptionArgument(_names, _valueName, _description, _default, key))
      }
      def |>(key: Symbol): Unit = |>(key.name)

      def name = | _
      def description = |% _
      def valueName = |^ _
      def default = |* _
      def key(key: String) = |>(key)
      def key(key: Symbol) = |>(key)
    }
  }

  def newOptional(name: String) = !(new OptionalBuilder(name))

  implicit def toOptionalBuilder(name: String) = new OptionalBuilder(name)

  /**
   * build an separator
   *
   *   ("---------------------" >>>)
   *   ("-" >>> 60)
   *
   *   ("=====================" >>>>)
   *   ("=" >>>> 60)
   *
   */
  class SeparatorBuilder(description: String) {
    def >>> {
      self.addArgument(Separator(description))
    }

    def >>>(number: Int) {
      self.addArgument(Separator(description * number))
    }

    def >>>> {
      self.addArgument(Separator(SeparatorBuilder.NL + description + SeparatorBuilder.NL))
    }

    def >>>>(number: Int) {
      self.addArgument(Separator(SeparatorBuilder.NL + (description * number) + SeparatorBuilder.NL))
    }
  }
  private object SeparatorBuilder {
    private val NL = System.getProperty("line.separator")
  }

  def newSeparator(description: String, number: Int = 1, multiLine: Boolean = false) =
    if(multiLine) new SeparatorBuilder(description).>>>>(number)
    else new SeparatorBuilder(description).>>>(number)

  implicit def toSeparatorBuilder(description: String) = new SeparatorBuilder(description)
}

// vim: set ts=2 sw=2 et:
