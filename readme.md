scopt v2
========

scopt v2 is a little command line options parsing library.

it's an fork from [jstrachan/scopt](http://github.com/jstrachan/scopt) with huge refactorings and reconstructions.

scopt v2 is completely incompatible with the former version. so don't wonder.


Usage
-----

Create an *ArgumentParser* and customise it with the options you need, passing in functions to process each option or argument.
Optionally an easy to use Configuration Map (`ConfigMap`) can be used to store an retrieve the configuration values.

    // we want to store three values
    class Configuration extends ConfigMap {
      lazy val verbose = get[Boolean]("verbose") getOrElse false
      lazy val outfile = get[String]("outfile") getOrElse "-"
      lazy val infile = ("infile", "").as[String]
    }

    // our argument parser which uses some ConfigMap
    case class SimpleParser(config: ConfigMap) extends ArgumentParser {
      override val programName = Some("SimpleExample")

      ! "-v" | "--verbose"   |% "active verbose output"            |> config.set("verbose")
      ! "-o" |^ "OUT" |* "-" |% "output filename, default: stdout" |> { config.set("outfile", _) }
      ("-" >>> 50)
      + "infile"             |% "input filename"                   |> config.set("infile")
    }

    val config = new Configuration
    if(SimpleParser(config).parse(args)) {
      // do stuff, e.g.
      println("verbose: " + c.verbose)
      println("outfile: " + c.outfile)
      println(" infile: " + c.infile)
    } else {
      // arguments are bad, usage message will have been displayed
    }

The above generates the following usage text:


    usage: SimpleExample [options] infile

    options:
      -v, --verbose   active verbose output
      -o OUT          output filename, default: stdout
      --------------------------------------------------
      infile          input filename


Building
--------

Use [sbt](http://code.google.com/p/simple-build-tool/) to build scopt v2.

    $ sbt
    $ > update
    $ > test
    $ > dist

To run some of the examples, switch to the `scopt-examples` subproject:

    $ > project scopt-examples
    $ > run


API
---

The `ConfigMap` provide some *setters* and *getters* to work with the stored data.

The *setters* are intended to be used inside our argument parser.

    def setters(config: ConfigMap) {
      config.set("key")("value")
      config.set("key" -> "value")
      config.set("key", "value")
    }

These *getters* are intended to be uses for our data accessor functions.

    class MyConfig extends ConfigMap {
      // getters
      lazy val str   = ("key", "default").as[String]
      lazy val bool  = ("key", false).as[Boolean]
      lazy val other = get[Int]("key") getOrElse 42
    }

The `ArgumentParser` provides a nice dsl to create the argument mappings. Two types of arguments
can be specified. Options (like -f, --bar) or positionals (like the input filename on the last position).

You can also specify separators, which separates the usage text.


### Positionals

    + "required"   |% "some description"    |> {String => Unit}
    ~ "optional"   |% "blah blah"           |> {String => Unit}

The `+` define a *required* positional argument, which can not be omited.</br>
A `~` defines a *optional* positional argument, which can be omited.

There can **never** be a required argument **after** an optional one. This will produce an
`BadArgumentOrderException` Exception.

Descriptions denoted with a `|%` are optional and can be omited.

The Action denoted with a `|>` is strongly required and always have the form `(String) => Unit`.

At the moment you can only have a known number of positional arguments. This may change.

#### Alternate syntax:

    newPositional("required").required.
                              description("description").
                              action(String => Unit)

    newPositional("optional").optional.
                              description("description").
                              action(String => Unit)


### Options

    ! "-f" | "--foo" |^ "valueName" |* "defaultValue" |% "description" |> {String => Unit}

Options are defined by using a starting `!`. After that, there can be any number of additional names
using a `|`. At least there is always one, sometimes more names for that option.

An option can be a flag or can have a value. Flags are things like `-f`, `--verbose` and so on. To define
such flags, omit the value name `|^`.<br/>
If you define a value name, the following variant are allowed: `-f value`, `-f=value` and `-f:value`. 
The delimiter (`:` and `=`) can be changed by overriding the member `optionDelimiters`.

If you don't define a default value with `|*`, the option must be specified. The default value can be
any type which is convertable to a string with the `toString` method.

Descriptions denoted with a `|%` are optional and can be omited.

The Action denoted with a `|>` is strongly required and always have the form `(String) => Unit`.

#### Alternate syntax:

    newOptional("-f").name("--foo").
                      valueName("valueName).
                      default("defaultValue").
                      description("description").
                      action(String => Unit)


### Separators

    ("---------------------" >>>)
    ("-" >>> 60)

the first form will produce exactly the given string, while in the second form the given string will
be multiplicated n times. (like `"/" * 60`)

    ("=====================" >>>>)
    ("=" >>>> 60)

these behave like the operators above, but will add a newline at the start and the end.

beware, those annoying parentheses are needed.

#### Alternate syntax:

    newSeparator("--------------------")
    newSeparator("-", 60)

    newSeparator("====================", multiLine = true)
    newSeparator("=", 60, true)



Extending ConfigMap
-------------------

Imagine you want to save and get key/value pairs in your `ConfigMap` via `-v key=value` arguments.

The only thing you must do is to provide a new type class instance of the trait `Reader[T]`.
In this example it's the `implicit object KeyValueReader`.

    case class KeyValuePair(key: String, value: String)
  
    class KeyValueConfig extends ConfigMap {
      // type class to read an KeyValuePair
      implicit object KeyValueReader extends Reader[KeyValuePair] {
        def read(value: String): KeyValuePair = value.indexOf('=') match {
            case n: Int if n >= 0 =>        
              val p = value.splitAt(n)        
              KeyValuePair(p._1, p._2)        
            case _ => throw new IllegalArgumentException("Expected a key=value pair")
          }
      } 
  
      lazy val pair = ("pair", KeyValuePair("", "")).as[KeyValuePair]
    }
  
    case class KeyValueParser(config: ConfigMap) extends ArgumentParser {
      override val programName = Some("KeyValueExample")
      
      ! "-k" |^ "Key=Value" |% "a key=value pair" |> config.set("pair")
    }


I think you got the idea.



Copyright and license
---------------------

scopt v2 is copyright &copy; 2010 [Rico Schiekel](http://downgra.de) and released under the [WTFPL](http://sam.zoy.org/wtfpl/).
See the accompanying license file for details.



Credits
-------

This code is based on work by Tim Perrett, Aaron Harnly's, James Strachan and all others in the commit history.



TODO
----

* Can we use the Reader[T] type classes also in the ArgumentParser to got some sort of input validation?
* Support unknown number of positional arguments.
* Add more scaladoc.

