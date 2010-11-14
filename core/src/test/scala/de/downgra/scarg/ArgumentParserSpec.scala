package de.downgra.scarg

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class ArgumentParserSpec extends FunSuite with ShouldMatchers {

  class NoHelpArgumentParser extends ArgumentParser(s => s) with DefaultHelpViewer {
    override val showErrors = false
  }

  test("empty argument list on empty option parser") {
    class OP extends NoHelpArgumentParser
    val op = new OP
    op.parse(List()).isRight should be (true)
  }

  test("non-empty argument list on empty option parser should fail") {
    class OP extends NoHelpArgumentParser
    val op = new OP
    op.parse(List("foo")).isLeft should be (true)
    op.parse(List("-foo")).isLeft should be (true)
    op.parse(List("--foo")).isLeft should be (true)
    op.parse(List("--foo", "bar")).isLeft should be (true)
  }

  test("single required positional argument") {
    class OP extends NoHelpArgumentParser {
      + "required" |% "description" |> 'required
    }
    val op = new OP

    op.positionalArguments should have length (1)
    op.positionalArguments(0) should have(
      'name ("required"),
      'description ("description"),
      'repeated (false),
      'optional (false)
    )

    val a = op.parse(Nil)
    a.isLeft should be (true)
    a.left.get.length should be (1)

    val b = op.parse(List("--foo"))
    b.isLeft should be (true)
    b.left.get.length should be (2)

    val c = op.parse(List("foo"))
    c.isRight should be (true)
    c.right.get should be (Map("required" -> List("foo")))
  }

  test("single optional positional argument") {
    class OP extends NoHelpArgumentParser {
      ~ "optional" |% "description" |> 'optional
    }
    val op = new OP

    op.positionalArguments should have length (1)
    op.positionalArguments(0) should have(
      'name ("optional"),
      'description ("description"),
      'repeated (false),
      'optional (true)
    )

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map())

    val b = op.parse(List("--foo"))
    b.isLeft should be (true)
    b.left.get.length should be (1)

    val c = op.parse(List("foo"))
    c.isRight should be (true)
    c.right.get should be (Map("optional" -> List("foo")))
  }

  test("repeated required positional argument") {
    class OP extends NoHelpArgumentParser {
      + "required" |% "description" |*> 'required
    }
    val op = new OP

    op.positionalArguments should have length (1)
    op.positionalArguments(0) should have(
      'name ("required"),
      'description ("description"),
      'repeated (true),
      'optional (false)
    )

    val a = op.parse(Nil)
    a.isLeft should be (true)
    a.left.get.length should be (1)

    val b = op.parse(List("--foo"))
    b.isLeft should be (true)
    b.left.get.length should be (2)

    val c = op.parse(List("foo"))
    c.isRight should be (true)
    c.right.get should be (Map("required" -> List("foo")))

    val d = op.parse(List("foo", "bar"))
    d.isRight should be (true)
    d.right.get should be (Map("required" -> List("foo", "bar")))
  }

  test("repeated optional positional argument") {
    class OP extends NoHelpArgumentParser {
      ~ "optional" |% "description" |*> 'optional
    }
    val op = new OP

    op.positionalArguments should have length (1)
    op.positionalArguments(0) should have(
      'name ("optional"),
      'description ("description"),
      'repeated (true),
      'optional (true)
    )

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("optional" -> Nil))

    val b = op.parse(List("--foo"))
    b.isLeft should be (true)
    b.left.get.length should be (1)

    val c = op.parse(List("foo"))
    c.isRight should be (true)
    c.right.get should be (Map("optional" -> List("foo")))

    val d = op.parse(List("foo", "bar"))
    d.isRight should be (true)
    d.right.get should be (Map("optional" -> List("foo", "bar")))
  }

  test("single flag argument") {
    class OP extends NoHelpArgumentParser {
      ! "-f" |% "description" |> 'flag
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("flag" -> List("false")))

    val b = op.parse(List("-f"))
    b.isRight should be (true)
    b.right.get should be (Map("flag" -> List("true")))
  }

  test("override flag default arguments") {
    class OP extends NoHelpArgumentParser {
      override val flagDefaults = ("foo", "bar")
      ! "-f" |% "description" |> 'flag
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("flag" -> List("bar")))

    val b = op.parse(List("-f"))
    b.isRight should be (true)
    b.right.get should be (Map("flag" -> List("foo")))
  }

  test("multiple flag arguments") {
    class OP extends NoHelpArgumentParser {
      ! "-a" |% "description1" |> 'a
      ! "-b" |% "description2" |> 'b
      ! "-c" |% "description3" |> 'c
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("a" -> List("false"), "b" -> List("false"), "c" -> List("false")))

    val b = op.parse(List("-a", "-b", "-c"))
    b.isRight should be (true)
    b.right.get should be (Map("a" -> List("true"), "b" -> List("true"), "c" -> List("true")))

    val c = op.parse(List("-a", "-c"))
    c.isRight should be (true)
    c.right.get should be (Map("a" -> List("true"), "b" -> List("false"), "c" -> List("true")))

    val d = op.parse(List("-c"))
    d.isRight should be (true)
    d.right.get should be (Map("a" -> List("false"), "b" -> List("false"), "c" -> List("true")))
  }

  test("single required option argument") {
    class OP extends NoHelpArgumentParser {
      ! "-f" |^ "FOO" |% "description" |> 'flag
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isLeft should be (true)
    a.left.get.length should be (1)

    val b = op.parse(List("-f"))
    a.isLeft should be (true)
    a.left.get.length should be (1)

    for(params <- List(List("-f", "value"), List("-f=value"), List("-f:value"))) {
      val c = op.parse(params)
      c.isRight should be (true)
      c.right.get should be (Map("flag" -> List("value")))
    }
  }

  test("multiple required option arguments") {
    class OP extends NoHelpArgumentParser {
      ! "-a" |^ "AAA" |% "description1" |> 'a
      ! "-b" |^ "BBB" |% "description2" |> 'b
      ! "-c" |^ "CCC" |% "description3" |> 'c
    }
    val op = new OP

    for(params <- List(Nil,
                       List("-a", "-b", "bbb", "-c=ccc"),
                       List("-b", "bbb", "-c=ccc"),
                       List("-b", "-c=ccc"))) {
      val a = op.parse(Nil)
      a.isLeft should be (true)
    }

    for(params <- List(List("-a", "aaa", "-b", "bbb", "-c", "ccc"),
                       List("-a:aaa", "-b", "bbb", "-c=ccc"))) {
      val e = op.parse(params)
      e.isRight should be (true)
      e.right.get should be (Map("a" -> List("aaa"),
                                 "b" -> List("bbb"),
                                 "c" -> List("ccc")))
    }
  }

  test("single flag with default value") {
    class OP extends NoHelpArgumentParser {
      ! "-f" |* "default" |> 'flag
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("flag" -> List("false")))

    val b = op.parse(List("-f"))
    b.isRight should be (true)
    b.right.get should be (Map("flag" -> List("true")))
  }

  test("single option with default value") {
    class OP extends NoHelpArgumentParser {
      ! "-f" |^ "FOO" |* "default" |> 'flag
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("flag" -> List("default")))

    val b = op.parse(List("-f", "foo"))
    b.isRight should be (true)
    b.right.get should be (Map("flag" -> List("foo")))
  }

  test("multiple options with default values") {
    class OP extends NoHelpArgumentParser {
      ! "-a" |^ "FOO" |* "default1"  |> 'a
      ! "-b" |^ "BAR" |* "default2"  |> 'b
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("a" -> List("default1"),
                               "b" -> List("default2")))

    val b = op.parse(List("-a", "foo"))
    b.isRight should be (true)
    b.right.get should be (Map("a" -> List("foo"),
                               "b" -> List("default2")))

    val c = op.parse(List("-b", "bar"))
    c.isRight should be (true)
    c.right.get should be (Map("a" -> List("default1"),
                               "b" -> List("bar")))

    val d = op.parse(List("-a=foo", "-b", "bar"))
    d.isRight should be (true)
    d.right.get should be (Map("a" -> List("foo"),
                               "b" -> List("bar")))

    val e = op.parse(List("-a", "-b", "bar"))
    e.isLeft should be (true)
    e.left.get.length should (be > 0)
  }

  test("flag and required positional") {
    class OP extends NoHelpArgumentParser {
      ! "-f"  |> 'f
      + "bar" |> 'p
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isLeft should be (true)
    a.left.get.length should (be > 0)

    for(params <- List(List("-f", "barbar"), List("barbar", "-f"))) {
      val b = op.parse(params)
      b.isRight should be (true)
      b.right.get should be (Map("f" -> List("true"), "p" -> List("barbar")))
    }
  }

  test("flag and required repeated positional") {
    class OP extends NoHelpArgumentParser {
      ! "-f"  |> 'f
      + "bar" |*> 'p
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isLeft should be (true)
    a.left.get.length should (be > 0)

    for(params <- List(List("-f", "bar", "foo"), List("bar", "foo", "-f"))) {
      val b = op.parse(params)
      b.isRight should be (true)
      b.right.get should be (Map("f" -> List("true"), "p" -> List("bar", "foo")))
    }
  }

  test("flag and repeated optional positional") {
    class OP extends NoHelpArgumentParser {
      ! "-f"  |> 'f
      ~ "bar" |*> 'p
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isRight should be (true)
    a.right.get should be (Map("f" -> List("false"), "p" -> Nil))

    for(params <- List(List("-f", "bar", "foo"), List("bar", "foo", "-f"))) {
      val b = op.parse(params)
      b.isRight should be (true)
      b.right.get should be (Map("f" -> List("true"), "p" -> List("bar", "foo")))
    }
  }

  test("optional and positional") {
    class OP extends NoHelpArgumentParser {
      ! "-f"  |^ "FOO" |> 'f
      + "bar"          |> 'p
    }
    val op = new OP

    val a = op.parse(Nil)
    a.isLeft should be (true)
    a.left.get.length should (be > 0)

    for(params <- List(List("barbar", "-f"), List("-f", "barbar"))) {
      val b = op.parse(params)
      b.isLeft should be (true)
      b.left.get.length should (be > 0)
    }

    for(params <- List(List("-f", "foo", "barbar"), List("barbar", "-f", "foo"))) {
      val c = op.parse(params)
      c.isRight should be (true)
      c.right.get should be (Map("f" -> List("foo"), "p" -> List("barbar")))
    }
  }

  test("double positional arguments") {
    class OP extends NoHelpArgumentParser {
      + "foo" |> 'a
      + "foo" |> 'b
    }

    evaluating { new OP } should produce [DoubleArgumentException]
  }

  test("required positional before optional positional") {
    class OP extends NoHelpArgumentParser {
      + "foo" |> 'a
      ~ "bar" |> 'b
    }

    new OP
  }

  test("required positional after optional positional") {
    class OP extends NoHelpArgumentParser {
      ~ "foo" |> 'a
      + "bar" |> 'b
    }

    evaluating { new OP } should produce [BadArgumentOrderException]
  }

  test("double option arguments") {
    class OP1 extends NoHelpArgumentParser {
      ! "-f" |> 'a
      ! "-f" |> 'b
    }
    class OP2 extends NoHelpArgumentParser {
      ! "-f" | "--foo" |> 'a
      ! "-b" | "--foo" |> 'b
    }
    evaluating { new OP1 } should produce [DoubleArgumentException]
    evaluating { new OP2 } should produce [DoubleArgumentException]
  }

  test("repeated position not the last argument specified") {
    class OP1 extends NoHelpArgumentParser {
      ~ "foo" |*> 'foo
      ~ "bar" |> 'bar
    }
    class OP2 extends NoHelpArgumentParser {
      + "foo" |*> 'foo
      + "bar" |> 'bar
    }
    evaluating { new OP1 } should produce [BadArgumentOrderException]
    evaluating { new OP2 } should produce [BadArgumentOrderException]
  }

  test("no optional arguments allowed after positionals") {
    class OP1 extends NoHelpArgumentParser {
      ~ "foo" |> 'foo
      ! "-b"  |> 'bar
    }
    class OP2 extends NoHelpArgumentParser {
      + "foo" |> 'foo
      ! "-b"  |> 'bar
    }
    evaluating { new OP1 } should produce [BadArgumentOrderException]
    evaluating { new OP2 } should produce [BadArgumentOrderException]
  }

  test("error on unknown argument") {
    class OP extends NoHelpArgumentParser {
      ! "-f" |> 'f
    }
    val op = new OP

    val a = op.parse(List("-b"))
    a.isLeft should be (true)
    a.left.get.length should (be > 0)
  }

  test("disable error on unknown argument") {
    class OP extends NoHelpArgumentParser {
      override val errorOnUnknownArgument = false
      ! "-f" |> 'f
    }
    val op = new OP

    val a = op.parse(List("-b"))
    a.isRight should be (true)
    a.right.get should be (Map("f" -> List("false")))
  }
}
