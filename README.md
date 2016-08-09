# ActionUnit
Action based JUnit[[0]] test runner library

# Installation
Actionunit requires Java SE7 or later.
Following is a maven coordinate for ActionUnit.

```xml

    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>actionunit</artifactId>
      <version>[1.0.1,)</version>
      <scope>test</test>
    </dependency>
```

Also it requires JUnit 4.12 or later. Please make sure you are using it in your dependencies.

# Usage

Following is an example of actionunit.

```java

    import com.github.dakusui.actionunit.Action;
    import com.github.dakusui.actionunit.ActionUnit;
    import com.github.dakusui.actionunit.visitors.ActionRunner;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    
    import static com.github.dakusui.actionunit.Actions.sequential;
    import static com.github.dakusui.actionunit.Actions.simple;
    
    
    @RunWith(ActionUnit.class)
    public class Example {
      @Retention(RetentionPolicy.RUNTIME)
      public @interface Print {
      }
    
      @ActionUnit.PerformWith({ Print.class, Test.class })
      public Action testMethod() {
        return sequential(
            simple(() -> System.out.println("Hello")),
            simple(() -> System.out.println("World"))
        );
      }
    
      @Print
      public void print(Action action) {
        System.out.println(action);
      }
    
      @Test
      public void run(Action action) {
        action.accept(new ActionRunner());
      }
    }

```

This will print out

```

    2 actions
    Hello
    World

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
