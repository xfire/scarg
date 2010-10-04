package de.downgra.scarg

trait Reader[T] {
  def read(value: String): T
}

// vim: set ts=2 sw=2 et:
