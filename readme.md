scarg
=====

scarg is a little command line argument parsing library.


Usage
-----

Create a configuration map and an *ArgumentParser* and customise it with the options you need. To simplify life,
use the `ConfigMap` for your configuration map to receive some nice power-ups like value type conversion. (but 
you can also use a simple `Map`)

    // we want to store three values, a boolean and two strings
    class Configuration(m: ValueMap) extends ConfigMap(m) {
      val verbose = ("verbose", false).as[Boolean]
      val outfile = ("outfile", "-").as[String]
      val infile = ("infile", "").as[String]
    }

    // our argument parser which uses a factory to create our Configuration
    case class SimpleParser() extends ArgumentParser(new Configuration(_))
                                 with DefaultHelpViewer {
      override val programName = Some("SimpleExample") // set the program name for the help text

      // define our expected arguments
      ! "-v" | "--verbose"   |% "active verbose output"            |> "verbose"
      ! "-o" |^ "OUT" |* "-" |% "output filename, default: stdout" |> 'outfile
      ("-" >>> 50)
      + "infile"             |% "input filename"                   |> 'infile
    }


    SimpleParser().parse(args) match {
      case Right(c) =>
        println("verbose: " + c.verbose)
        println("outfile: " + c.outfile)
        println(" infile: " + c.infile)
      case Left(xs) =>
        // arguments are bad, usage message will have been displayed automagically
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

Use [sbt](http://code.google.com/p/simple-build-tool/) to build scarg.

    $ sbt
    $ > update
    $ > test
    $ > dist

To run some of the examples, switch to the `scarg-examples` subproject:

    $ > project scarg-examples
    $ > run
    $ > run -v -o outfile.txt infile.txt


API
---

Simply extend the class `ArgumentParser` and provide a factory which will transform a `ValueMap` into
your own configuration mapping. The `ConfigMap` can be used for this task.

The `ValueMap` has the type `Map[String, List[String]]`, ergo maps a key (which is a string) to a list
of string values (if parameters are given multiple times).

### ConfigMap

The `ConfigMap` provide some data converters which can be used to quickly create a container with your
arguments with the correct type.

    class MyConfig(m: ValueMap) extends ConfigMap(m) {
      val verbose = ("verbose", false).as[Boolean]
      val outfile = ("outfile", "-").as[String]
      val infile = ("infile", "").as[String]
    }

If you define `val`'s, type conversion errors will be generated within the parsing process. Lazy `val`'s
or `def`'s will throw exceptions on the first access.

### Argument Parser

The `ArgumentParser` provides a nice dsl to create the argument mappings. Two types of arguments
can be specified. Options (like `-f`, `--bar`) or positionals (like the input filename on the last position).

You can also specify separators to separates the usage text.

#### Positionals

    + "required"   |% "some description"    |> "key"
    + "required"   |% "some description"    |*> "key"
    ~ "optional"   |% "blah blah"           |> 'key
    ~ "optional"   |% "blah blah"           |*> 'key

The `+` define a *required* positional argument, which can not be omited.

A `~` defines a *optional* positional argument, which can be omited.

There can **never** be a required argument **after** an optional one. This will produce an
`BadArgumentOrderException` Exception.

Descriptions denoted with a `|%` are optional and can be omited.

The key denoted with a `|>` is required and set the name under which the parsed value
is inserted into the map of parsed values.

A key denoted with a `|*>` marks repeated positionals (e.g. unlimited number of input files).

A key can be a String or a Symbol.

##### Alternate syntax:

    newPositional("required").required.
                              description("description").
                              key("key")
    newPositional("required").required.
                              description("description").
                              key("key", repeated = false)

    newPositional("optional").optional.
                              description("description").
                              key('key, true)


#### Options

    ! "-f" | "--foo" |^ "valueName" |* "defaultValue" |% "description" |> 'key

Options are defined by using a starting `!`. After that, there can be any number of additional names
using a `|`. At least there is one, sometimes more names for that option.

An option can be a flag or it can have a value. Flags are things like `-f`, `--verbose` and so on. To define
such flags, omit the value name `|^`.

If you define a value name `myValue`, the following variant are allowed: `-f myValue`, `-f=myValue` and `-f:myValue`. 
The delimiter (`:` and `=`) can be changed by overriding the member `optionDelimiters`.

If you don't define a default value with `|*`, the option must be specified. The default value can be
any type which is convertable to a string with the `toString` method.

Descriptions denoted with a `|%` are optional and can be omited.

The key denoted with a `|>` is required and can be a String or a Symbol. It's used as key for the map of parsed
values.

##### Alternate syntax:

    newOptional("-f").name("--foo").
                      valueName("valueName).
                      default("defaultValue").
                      description("description").
                      key('key)


#### Separators

    ("---------------------" >>>)
    ("-" >>> 60)

the first form will produce exactly the given string, while in the second form the given string will
be multiplicated n times. (like `"/" * 60`)

    ("=====================" >>>>)
    ("=" >>>> 60)

these behave like the operators above, but will add a newline at the start and the end.

beware, those annoying parentheses are needed.

##### Alternate syntax:

    newSeparator("--------------------")
    newSeparator("-", 60)

    newSeparator("====================", multiLine = true)
    newSeparator("=", 60, true)


### Help Viewer

The generation of the help and usage text can be specified by providing an implementation of the `HelpViewer` trait.
A default implementation is provided with the `DefaultHelpViewer` trait, which can be mixed into the `ArgumentParser`
class.
If you don't want output, you can use the `SilentHelpViewer` or create an own by extending the `HelpViewer` trait.

The `DefaultHelpViewer` can be customized by overriding it's `val`'s like `INDENT`, `USAGE_HEADER`,
`UNKNOWN_ARGUMENT`, ... in your parser implementation.
Also the default output to `stderr` can be changed by overwriting the `output(s: String)` method.


Extending ConfigMap
-------------------

Imagine you want to get key/value pairs in your `ConfigMap` via `-v key=value` arguments.

The only thing you must do is to provide a new type class instance of the trait `Reader[T]`.
In this example it's the `implicit object KeyValueReader`.

    case class KeyValuePair(key: String, value: String)
  
    class KeyValueConfig(m: ValueMap) extends ConfigMap(m) {
      // type class to read an KeyValuePair
      implicit object KeyValueReader extends Reader[KeyValuePair] {
        def read(value: String): KeyValuePair = value.indexOf('=') match {
            case n: Int if n >= 0 =>        
              val p = value.splitAt(n)        
              KeyValuePair(p._1, p._2)        
            case _ => throw new IllegalArgumentException("Expected a key=value pair")
          }
      } 
  
      val pair = ("pair", KeyValuePair("", "")).as[KeyValuePair]
    }
  
    case class KeyValueParser() extends ArgumentParser(new KeyValueConfig(_)) with DefaultHelpViewer {
      override val programName = Some("KeyValueExample")
      
      ! "-k" |^ "Key=Value" |% "a key=value pair" |> config.set("pair")
    }


I think you got the idea.



Copyright and license
---------------------

scarg is copyright &copy; 2010 [Rico Schiekel](http://downgra.de) and released under the [WTFPL](http://sam.zoy.org/wtfpl/).
See the accompanying license file for details.



TODO
----

* nothing ;)
