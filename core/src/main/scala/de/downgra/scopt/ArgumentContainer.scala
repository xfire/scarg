package de.downgra.scarg

import collection.mutable.Buffer

trait ArgumentContainer {

  private[scarg] val arguments: Buffer[Argument]

  private[scarg] def addArgument(arg: Argument): Unit
}
