package com.github.dakusui.actionunit.ut.utils;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import org.junit.Test;

import java.util.TreeMap;

import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class StableTemplatingUtilsUnitTest {
  @Test
  public void givenNoVariable$whenTemplating$thenTemplated() {
    assertThat(
        StableTemplatingUtils.template(
            "Hello world, everyone.",
            new TreeMap<String, Object>() {{
            }}
        ),
        asString().equalTo("Hello world, everyone.").$()
    );
  }

  @Test
  public void givenOneVariable$whenTemplating$thenTemplated() {
    assertThat(
        StableTemplatingUtils.template(
            "Hello {{A}}, everyone.",
            new TreeMap<String, Object>() {{
              this.put("{{A}}", "world");
            }}
        ),
        asString().equalTo("Hello world, everyone.").$()
    );
  }

  @Test
  public void givenTwoVariables$whenTemplating$thenTemplated() {
    assertThat(
        StableTemplatingUtils.template(
            "Hello {{A}}, {{B}}.",
            new TreeMap<String, Object>() {{
              put("{{A}}", "world");
              put("{{B}}", "everyone");
            }}
        ),
        asString().equalTo("Hello world, everyone.").$()
    );
  }
}
