# ActionUnit
Action based JUnit[[0]] test runner library

[![Build Status](https://travis-ci.org/dakusui/actionunit.svg?branch=master)](https://travis-ci.org/dakusui/actionunit)

# Installation
The latest version of ActionUnit requires Java SE8 or later.
Following is a maven coordinate for ActionUnit.

```xml

    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>actionunit</artifactId>
      <version>[3.0.0,)</version>
      <scope>test</scope>
    </dependency>
```

Also it requires JUnit 4.12 or later. Please make sure you are using it in your dependencies.

# For what is it useful?
Suppose that you want to define your tests in your own DSTL, domain specific testing 
language, and want to implement a test runner which performs the test cases defined 
in it.

How to define test cases, how to load them, and how to perform them are all independent
concerns.

```ActionUnit``` takes care of the last part among them, "how to perform them".

# Usage

Following is an example of actionunit.

```java

  class Example implements ActionFactory {
    void runAction() {
        Action action = forEachOf(
            "Hello", "world", "!"
        ).concurrently(
        ).perform(
            s -> sequential(
                simple(
                    "print {s}",
                    () -> System.out.println("<" + s.get() + ">")
                ),
                simple(
                    "do something"
                    // ...
                )
            )
        );
        new ReportingActionPerformer.Builder(givenAction).to(Writer.Std.ERR).build().perform();
    }
  }
```

This will print out something like

```

  <world>
  <Hello>
  <!>
```
to stdout, while following is written to stderr

```
  [o]ForEach
    [ooo]Sequential (2 actions)
      [ooo]print {s}
      [ooo]add {s} to 'out'
```

More examples are found here[[1]].
And API reference is found here [[2]].

# References
* [0] "JUnit"
* [1] "ActionUnit examples"
* [2] "ActionUnit API reference"

[0]: http://junit.org/junit4/
[1]: https://github.com/dakusui/actionunit/tree/master/src/test/java/com/github/dakusui/actionunit/examples
[2]: https://dakusui.github.io/actionunit/
