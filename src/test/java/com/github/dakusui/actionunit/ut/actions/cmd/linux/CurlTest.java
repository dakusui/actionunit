package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.Curl;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.printables.PrintableFunctionals;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.*;
import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.printables.PrintableFunctionals.printableFunction;

public class CurlTest extends CommanderTestBase {
  @Ignore
  @Test
  public void test1() {
    Curl curl = curl().get().url("https://www.github.com/").urlEncodedData(immediateOf("Hello world"));
    System.out.println(curl.buildCommandLineComposer().format());
    performAction(
        ActionSupport.forEach("i", curl.toStreamGenerator())
            .perform(b -> leaf(writeTo(System.out::println, printableFunction(b::contextVariable).describe("contextVariable"))))
    );
  }

  @Test
  public void test2() {
    Curl curl = curl()
        .get()
        .url("https://www.github.com")
        .urlEncodedData("Hello world");
    assertThat(
        curl,
        asString(call("buildCommandLineComposer").andThen("format").$())
            .equalTo("curl -X GET quoteWith['](https://www.github.com) --data-urlencode quoteWith['](Hello world)")
            .$()
    );
  }

  @Test
  public void test3() {
    Curl curl = curl()
        .put()
        .insecure()
        .silent()
        .includeHeader()
        .url(immediateOf("https://www.github.com"))
        .rawData("Hello world");
    assertThat(
        curl,
        asString(call("buildCommandLineComposer").andThen("format").$())
            .equalTo("curl -X PUT --insecure -s -i quoteWith['](https://www.github.com) --data-raw quoteWith['](Hello world)")
            .$()
    );
  }

  @Test
  public void test4() {
    Curl curl = curl()
        .post()
        .insecure()
        .silent()
        .headerOnly()
        .url(immediateOf("https://www.github.com"))
        .asciiData("Hello world");
    assertThat(
        curl,
        asString(call("buildCommandLineComposer").andThen("format").$())
            .equalTo("curl -X POST --insecure -s -I quoteWith['](https://www.github.com) --data quoteWith['](Hello world)")
            .$()
    );
  }

  @Test
  public void test5() {
    Curl curl = curl()
        .post()
        .insecure()
        .silent()
        .headerOnly()
        .url(immediateOf("https://www.github.com"))
        .binaryData("Hello world");
    assertThat(
        curl,
        asString(call("buildCommandLineComposer").andThen("format").$())
            .equalTo("curl -X POST --insecure -s -I quoteWith['](https://www.github.com) --data-binary quoteWith['](Hello world)")
            .$()
    );
  }

  @Test
  public void test6() {
    Curl curl = curl()
        .delete()
        .insecure()
        .silent()
        .headerOnly()
        .url(immediateOf("https://www.github.com"))
        .asciiData(immediateOf("Hello world"));
    assertThat(
        curl,
        asString(call("buildCommandLineComposer").andThen("format").$())
            .equalTo("curl -X DELETE --insecure -s -I quoteWith['](https://www.github.com) --data quoteWith['](Hello world)")
            .$()
    );
  }
}
