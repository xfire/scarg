package org.github.scopt

import collection.mutable.Buffer

trait ArgumentContainer {

  private[scopt] val arguments: Buffer[Argument]

  private[scopt] def addArgument(arg: Argument): Unit
}
