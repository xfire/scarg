package org.github.scopt

trait ArgumentBuilders {
  self: OptionParser =>

  /**
   * build a positional argument. the following forms are allowed:
   * required positional arguments:
   *   + "required" --> {action => action}
   *   + "required" % "description" --> {action => action}
   *
   * optional positional arguments:
   *   * "optional" --> {action => action}
   *   * "optional" % "description" --> {action => action}
   */
  class PositionalBuilder(name: String) {
    var description = ""
    var optional = false

    class Builder {
      def %(desc: String) = {
        description = desc
        this
      }
      def -->(f: String => Unit) = {
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

  // - "f" % 'description' --> {action => action}
  // - "f" -- "foo" % 'description' --> {action => action}

}

// vim: set ts=2 sw=2 et:
