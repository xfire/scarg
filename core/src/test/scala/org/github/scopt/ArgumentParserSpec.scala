package org.github.scopt

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class ArgumentParserSpec extends FunSuite with ShouldMatchers {

  test("empty argument list on empty option parser") {
    class OP extends ArgumentParser
    val op = new OP
    op.parse(List()) should be (true)
  }

  test("non-empty argument list on empty option parser should fail") {
    class OP extends ArgumentParser
    val op = new OP
    op.parseRaw(List("foo")) should not be ('empty)
    op.parseRaw(List("-foo")) should not be ('empty)
    op.parseRaw(List("--foo")) should not be ('empty)
    op.parseRaw(List("--foo", "bar")) should not be ('empty)
  }

  test("single required positional argument") {
    class OP extends ArgumentParser {
      var V: Option[String] = None
      + "required" |% "description" |> {s => V = Some(s)}
    }
    val op = new OP

    op.positionalArguments should have length (1)
    op.positionalArguments(0) should have(
      'name ("required"),
      'description ("description"),
      'optional (false)
    )

    op.parseRaw(Nil) should not be ('empty)
    op.V should be (None)
    op.parseRaw(List("--foo")) should not be ('empty)
    op.V should be (None)
    op.parseRaw(List("foo")) should be ('empty)
    op.V should be (Some("foo"))
  }

  test("single optional positional argument") {
    class OP extends ArgumentParser {
      var V: Option[String] = None
      ~ "optional" |% "description" |> {s => V = Some(s)}
    }
    val op = new OP

    op.positionalArguments should have length (1)
    op.positionalArguments(0) should have(
      'name ("optional"),
      'description ("description"),
      'optional (true)
    )

    op.parseRaw(Nil) should be ('empty)
    op.V should be (None)
    op.parseRaw(List("--foo")) should not be ('empty)
    op.V should be (None)
    op.parseRaw(List("foo")) should be ('empty)
    op.V should be (Some("foo"))
  }

  test("single flag argument") {
    class OP extends ArgumentParser {
      var V: Option[String] = None
      ! "-f" |% "description" |> {s => V = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should be ('empty)
    op.V should be (Some("false")); op.V = None
    op.parseRaw(List("-f")) should be ('empty)
    op.V should be (Some("true"))
  }

  test("multiple flag arguments") {
    class OP extends ArgumentParser {
      var VA: Option[String] = None
      var VB: Option[String] = None
      var VC: Option[String] = None
      ! "-a" |% "description1" |> {s => VA = Some(s)}
      ! "-b" |% "description2" |> {s => VB = Some(s)}
      ! "-c" |% "description3" |> {s => VC = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should be ('empty)
    op.VA should be (Some("false"))
    op.VB should be (Some("false"))
    op.VC should be (Some("false"))
    op.parseRaw(List("-a", "-b", "-c")) should be ('empty)
    op.VA should be (Some("true")); op.VA = None
    op.VB should be (Some("true")); op.VB = None
    op.VC should be (Some("true")); op.VC = None
    op.parseRaw(List("-a", "-c")) should be ('empty)
    op.VA should be (Some("true")); op.VA = None
    op.VB should be (Some("false")); op.VB = None
    op.VC should be (Some("true")); op.VC = None
    op.parseRaw(List("-c")) should be ('empty)
    op.VA should be (Some("false")); op.VA = None
    op.VB should be (Some("false")); op.VB = None
    op.VC should be (Some("true")); op.VC = None
  }

  test("single required option argument") {
    class OP extends ArgumentParser {
      var V: Option[String] = None
      ! "-f" |^ "FOO" |% "description" |> {s => V = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should not be ('empty)
    op.V should be (None)
    op.parseRaw(List("-f")) should not be ('empty)
    op.V should be (None)
    op.parseRaw(List("-f", "value")) should be ('empty)
    op.V should be (Some("value")); op.V = None
    op.parseRaw(List("-f=value")) should be ('empty)
    op.V should be (Some("value")); op.V = None
    op.parseRaw(List("-f:value")) should be ('empty)
    op.V should be (Some("value")); op.V = None
  }

  test("multiple required option arguments") {
    class OP extends ArgumentParser {
      var VA: Option[String] = None
      var VB: Option[String] = None
      var VC: Option[String] = None
      ! "-a" |^ "AAA" |% "description1" |> {s => VA = Some(s)}
      ! "-b" |^ "BBB" |% "description2" |> {s => VB = Some(s)}
      ! "-c" |^ "CCC" |% "description3" |> {s => VC = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should not be ('empty)
    op.VA should be (None)
    op.VB should be (None)
    op.VC should be (None)
    op.parseRaw(List("-a", "aaa", "-b", "bbb", "-c", "ccc")) should be ('empty)
    op.VA should be (Some("aaa")); op.VA = None
    op.VB should be (Some("bbb")); op.VB = None
    op.VC should be (Some("ccc")); op.VC = None
    op.parseRaw(List("-a:aaa", "-b", "bbb", "-c=ccc")) should be ('empty)
    op.VA should be (Some("aaa")); op.VA = None
    op.VB should be (Some("bbb")); op.VB = None
    op.VC should be (Some("ccc")); op.VC = None
    op.parseRaw(List("-a", "-b", "bbb", "-c=ccc")) should not be ('empty)
    op.parseRaw(List("-b", "bbb", "-c=ccc")) should not be ('empty)
    op.parseRaw(List("-b", "-c=ccc")) should not be ('empty)
  }

  test("single flag with default value") {
    class OP extends ArgumentParser {
      var V: Option[String] = None
      ! "-f" |* "default"  |> {s => V = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should be ('empty)
    op.V should be (Some("false"))
    op.parseRaw(List("-f")) should be ('empty)
    op.V should be (Some("true"))
  }

  test("single option with default value") {
    class OP extends ArgumentParser {
      var V: Option[String] = None
      ! "-f" |^ "FOO" |* "default"  |> {s => V = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should be ('empty)
    op.V should be (Some("default"))
    op.parseRaw(List("-f", "foo")) should be ('empty)
    op.V should be (Some("foo"))
  }

  test("multiple options with default values") {
    class OP extends ArgumentParser {
      var VA: Option[String] = None
      var VB: Option[String] = None
      ! "-f" |^ "FOO" |* "default1"  |> {s => VA = Some(s)}
      ! "-b" |^ "BAR" |* "default2"  |> {s => VB = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should be ('empty)
    op.VA should be (Some("default1")); op.VA = None
    op.VB should be (Some("default2")); op.VB = None
    op.parseRaw(List("-f", "foo")) should be ('empty)
    op.VA should be (Some("foo")); op.VA = None
    op.VB should be (Some("default2")); op.VB = None
    op.parseRaw(List("-b", "bar")) should be ('empty)
    op.VA should be (Some("default1")); op.VA = None
    op.VB should be (Some("bar")); op.VB = None
    op.parseRaw(List("-f", "foo", "-b=bar")) should be ('empty)
    op.VA should be (Some("foo")); op.VA = None
    op.VB should be (Some("bar")); op.VB = None
    op.parseRaw(List("-f", "-b=bar")) should not be ('empty)
  }

  test("flag and positional") {
    class OP extends ArgumentParser {
      var VA: Option[String] = None
      var VB: Option[String] = None
      ! "-f"  |> {s => VA = Some(s)}
      + "bar" |> {s => VB = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should not be ('empty)
    op.VA should be (Some("false")); op.VA = None
    op.VB should be (None); op.VB = None
    op.parseRaw(List("-f", "barbar")) should be ('empty)
    op.VA should be (Some("true")); op.VA = None
    op.VB should be (Some("barbar")); op.VB = None
    op.parseRaw(List("barbar", "-f")) should be ('empty)
    op.VA should be (Some("true")); op.VA = None
    op.VB should be (Some("barbar")); op.VB = None
  }

  test("optional and positional") {
    class OP extends ArgumentParser {
      var VA: Option[String] = None
      var VB: Option[String] = None
      ! "-f"  |^ "FOO" |> {s => VA = Some(s)}
      + "bar"          |> {s => VB = Some(s)}
    }
    val op = new OP

    op.parseRaw(Nil) should not be ('empty)
    op.VA should be (None); op.VA = None
    op.VB should be (None); op.VB = None
    op.parseRaw(List("-f", "foo", "barbar")) should be ('empty)
    op.VA should be (Some("foo")); op.VA = None
    op.VB should be (Some("barbar")); op.VB = None
    op.parseRaw(List("barbar", "-f", "foo")) should be ('empty)
    op.VA should be (Some("foo")); op.VA = None
    op.VB should be (Some("barbar")); op.VB = None
    op.parseRaw(List("barbar", "-f")) should not be ('empty)
    op.parseRaw(List("-f", "barbar")) should not be ('empty)
  }

  test("double positional arguments") {
    class OP extends ArgumentParser {
      + "foo" |> {s => s}
      + "foo" |> {s => s}
    }

    evaluating { new OP } should produce [DoubleArgumentException]
  }

  test("required positional after optional positional") {
    class OP extends ArgumentParser {
      ~ "foo" |> {s => s}
      + "bar" |> {s => s}
    }

    evaluating { new OP } should produce [BadArgumentOrderException]
  }

  test("double option arguments") {
    class OP1 extends ArgumentParser {
      ! "-f" |> {s => s}
      ! "-f" |> {s => s}
    }
    class OP2 extends ArgumentParser {
      ! "-f" | "--foo" |> {s => s}
      ! "-b" | "--foo" |> {s => s}
    }
    evaluating { new OP1 } should produce [DoubleArgumentException]
    evaluating { new OP2 } should produce [DoubleArgumentException]
  }

  test("error on unknown argument") {
    class OP extends ArgumentParser {
      ! "-f" |> {s => s}
    }
    val op = new OP
    op.parseRaw(List("-b")) should not be ('empty)
  }

  test("disable error on unknown argument") {
    class OP extends ArgumentParser {
      override val errorOnUnknownArgument = false
      ! "-f" |> {s => s}
    }
    val op = new OP
    op.parseRaw(List("-b")) should be ('empty)
  }
}
