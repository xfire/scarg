package de.downgra.scarg

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import collection.mutable.ListBuffer

class ArgumentBuildersSpec extends FunSuite with ShouldMatchers {

  trait TestContainer extends ArgumentContainer {
    override val arguments = new ListBuffer[Argument]
    override def addArgument(arg: Argument) = arguments += arg
  }

  test("complete positional") {
    object Test extends TestContainer with ArgumentBuilders {
      + "required1" |% "description1" |> 'key
      ~ "optional" |% "description2" |> 'key
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('name ("required1"), 'description ("description1"), 'optional (false),
                                   'repeated(false))
    Test.arguments(1) should have ('name ("optional"), 'description ("description2"), 'optional (true),
                                   'repeated(false))
  }

  test("complete positional alternate syntax") {
    object Test extends TestContainer with ArgumentBuilders {
      positional("required1").required description("description1") key('key)
      positional("optional").optional.
                             description("description2").
                             key('key)
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('name ("required1"), 'description ("description1"), 'optional (false),
                                   'repeated(false))
    Test.arguments(1) should have ('name ("optional"), 'description ("description2"), 'optional (true),
                                   'repeated(false))
  }

  test("positional without description") {
    object Test extends TestContainer with ArgumentBuilders {
      + "required1" |> 'key
      ~ "required2" |> 'key
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('name ("required1"), 'description (""), 'optional (false),
                                   'repeated(false))
    Test.arguments(1) should have ('name ("required2"), 'description (""), 'optional (true),
                                   'repeated(false))
  }

  test("repeated positional") {
    object Test extends TestContainer with ArgumentBuilders {
      + "required" |*> 'key
      ~ "optional" |*> 'key
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('name ("required"), 'description (""), 'optional (false),
                                   'repeated (true))
    Test.arguments(1) should have ('name ("optional"), 'description (""), 'optional (true),
                                   'repeated (true))
  }


  test("complete option") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" | "--foo" |^ "valueName1" |* "defaultValue1" |% "description1" |> 'key
      ! "--oof" | "-o" |% "description2" |* "defaultValue2" |^ "valueName2" |> 'key
      ! "-b" |^ "valueName3" |* "defaultValue3" |% "description3" |> 'key
    }

    Test.arguments should have length (3)
    Test.arguments(0) should have ('names (List("-f", "--foo")), 'valueName (Some("valueName1")),
                                   'default (Some("defaultValue1")), 'description ("description1"))
    Test.arguments(1) should have ('names (List("--oof", "-o")), 'valueName (Some("valueName2")),
                                   'default (Some("defaultValue2")), 'description ("description2"))
    Test.arguments(2) should have ('names (List("-b")), 'valueName (Some("valueName3")),
                                   'default (Some("defaultValue3")), 'description ("description3"))
  }

  test("complete option alternate syntax") {
    object Test extends TestContainer with ArgumentBuilders {
      optional("-f") name("--foo") valueName("valueName1") default("defaultValue1") description("description1") key('key)
      optional("--oof").name("-o").
                        valueName("valueName2").
                        default("defaultValue2").
                        description("description2").
                        key('key)
      optional("-b") valueName("valueName3") default("defaultValue3") description("description3") key('key)
    }

    Test.arguments should have length (3)
    Test.arguments(0) should have ('names (List("-f", "--foo")), 'valueName (Some("valueName1")),
                                   'default (Some("defaultValue1")), 'description ("description1"))
    Test.arguments(1) should have ('names (List("--oof", "-o")), 'valueName (Some("valueName2")),
                                   'default (Some("defaultValue2")), 'description ("description2"))
    Test.arguments(2) should have ('names (List("-b")), 'valueName (Some("valueName3")),
                                   'default (Some("defaultValue3")), 'description ("description3"))
  }

  test("option without description") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" | "--foo" |^ "valueName1" |* "defaultValue1" |> 'key
      ! "--bar" |^ "valueName2" |* "defaultValue2" |> 'key
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('names (List("-f", "--foo")), 'valueName (Some("valueName1")),
                                   'default (Some("defaultValue1")), 'description (""))
    Test.arguments(1) should have ('names (List("--bar")), 'valueName (Some("valueName2")),
                                   'default (Some("defaultValue2")), 'description (""))
  }

  test("option without default value") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" | "--foo" |^ "valueName1" |% "description1" |> 'key
      ! "-b" |% "description2" |^ "valueName2" |> 'key
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('names (List("-f", "--foo")), 'valueName (Some("valueName1")),
                                   'default (None), 'description ("description1"))
    Test.arguments(1) should have ('names (List("-b")), 'valueName (Some("valueName2")),
                                   'default (None), 'description ("description2"))
  }

  test("option without value name") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" | "--foo" |* "defaultValue1" |% "description1" |> 'key
      ! "--bar" |% "description2" |* "defaultValue2" |> 'key
    }

    Test.arguments should have length (2)
    Test.arguments(0) should have ('names (List("-f", "--foo")), 'valueName (None),
                                   'default (Some("defaultValue1")), 'description ("description1"))
    Test.arguments(1) should have ('names (List("--bar")), 'valueName (None),
                                   'default (Some("defaultValue2")), 'description ("description2"))
  }

  test("minimal option") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" |> 'key
    }

    Test.arguments should have length (1)
    Test.arguments(0) should have ('names (List("-f")), 'valueName (None),
                                   'default (None), 'description (""))
  }

  test("lot option names") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" | "-foo" | "--bar" | "--blubblub" |> 'key
    }

    Test.arguments should have length (1)
    Test.arguments(0) should have ('names (List("-f", "-foo", "--bar", "--blubblub")), 'valueName (None),
                                   'default (None), 'description (""))
  }

  test("option non-string default values") {
    object Test extends TestContainer with ArgumentBuilders {
      ! "-f" |* 42 |> 'key
      ! "-d" |* 23.42 |> 'key
      ! "-b" |* true |> 'key
    }

    Test.arguments should have length (3)
    Test.arguments(0) should have ('names (List("-f")), 'valueName (None),
                                   'default (Some("42")), 'description (""))
    Test.arguments(1) should have ('names (List("-d")), 'valueName (None),
                                   'default (Some("23.42")), 'description (""))
    Test.arguments(2) should have ('names (List("-b")), 'valueName (None),
                                   'default (Some("true")), 'description (""))
  }

  test("separators") {
    object Test extends TestContainer with ArgumentBuilders {
      ("---------------------" >>>)
      ("-" >>> (60))
      ("=====================" >>>>)
      ("=" >>>> (60))
    }
    val NL = System.getProperty("line.separator")

    Test.arguments should have length (4)
    Test.arguments(0) should have ('description("---------------------"))
    Test.arguments(1) should have ('description("-" * 60))
    Test.arguments(2) should have ('description(NL + "=====================" + NL))
    Test.arguments(3) should have ('description(NL + ("=" * 60) + NL))
  }

  test("separators alternate syntax") {
    object Test extends TestContainer with ArgumentBuilders {
      separator("---------------------")
      separator("-", 60)
      separator("=====================", multiLine = true)
      separator("=", 60, true)
    }
    val NL = System.getProperty("line.separator")

    Test.arguments should have length (4)
    Test.arguments(0) should have ('description("---------------------"))
    Test.arguments(1) should have ('description("-" * 60))
    Test.arguments(2) should have ('description(NL + "=====================" + NL))
    Test.arguments(3) should have ('description(NL + ("=" * 60) + NL))
  }
}
