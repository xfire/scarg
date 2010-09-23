package org.github.scopt

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class OptionParserSpec extends FunSuite with ShouldMatchers {

  test("empty argument list on empty option parser") {
    class OP extends OptionParser
    val op = new OP
    op.parse(List()) should be (true)
  }

  test("non-empty argument list on empty option parser should fail") {
    class OP extends OptionParser
    val op = new OP
    op.parseRaw(List("foo")) should not be ('empty)
    op.parseRaw(List("-foo")) should not be ('empty)
    op.parseRaw(List("--foo")) should not be ('empty)
    op.parseRaw(List("--foo", "bar")) should not be ('empty)
  }

  test("single positional argument should parse") (pending)
}
