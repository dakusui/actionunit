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
      <version>[5.2.0,)</version>
      <scope>test</scope>
    </dependency>
```

Also it requires JUnit 4.12 or later. Please make sure you are using it in your dependencies.

# For what is it useful?
Suppose that you want to define your tests in your own DSTL, domain specific testing 
language, and want to implement a test runner which performs the test cases defined 
in it.

How to define test cases, how to parse them, and how to structure and perform them
are all independent concerns.

```ActionUnit``` takes care of the last two parts among them "how to structure and
perform them".

# Usage

Following is an example of actionunit.

```java

import static com.github.dakusui.actionunit.core.ActionSupport.*

public class HelloActionUnit {
  @Test
  public void helloActionUnit() {
    List<String> out = new LinkedList<>();
    Action action = forEach(
        "i",
        () -> Stream.of("Hello", "world", "!")
    ).perform(
        sequential(
            simple(
                "print {s}",
                (c) -> System.out.println("<" + c.valueOf("i") + ">")
            ),
            simple(
                "add {s} to 'out'",
                (c) -> out.add("'" + c.valueOf("i") + "'")
            )));

    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
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

[o]helloActionUnit
  [o]ForEach (CONCURRENTLY) [Hello, world, !]
    [ooo]Sequential (1 actions)
      [ooo]print {i}
```

This shows how actions are structured and whether each of them finished normally 
or not. ```o``` inside brackets represent how many times they are executed and 
finished normally. As you see, an action ```print {i}``` was executed three times 
successfully (```[ooo]```). If one a run of an action fails it will be shown as
```E``` or ```F```(the latter is for ```AssertionError```).


More examples are found here[[1]].
And API reference is found here [[2]].

# References
* [0] "JUnit"
* [1] "ActionUnit examples"
* [2] "ActionUnit API reference"

[0]: http://junit.org/junit4/
[1]: https://github.com/dakusui/actionunit/tree/master/src/test/java/com/github/dakusui/actionunit/examples
[2]: https://dakusui.github.io/actionunit/
