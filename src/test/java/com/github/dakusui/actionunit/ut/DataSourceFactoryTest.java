package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.DataSource;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import java.util.Iterator;

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DataSourceFactoryTest {
  @Test
  public void givenCustomDataSourceFactoryBackedByNonCollection$whenSizeQueried$thenMinus1Returned() {
    // given
    DataSource.Factory<String> dataSourceFactory = new DataSource.Factory.Base<String>() {
      @Override
      protected Iterable<String> iterable(Context context) {
        return new Iterable<String>() {
          @Override
          public Iterator<String> iterator() {
            return asList("Hello", "World").iterator();
          }
        };
      }
    };
    // when and then
    assertEquals(-1, dataSourceFactory.size());

    // when iterates
    TestUtils.Out out = new TestUtils.Out();
    for (String each : dataSourceFactory.create(TestUtils.DUMMY_CONTEXT)) {
      out.writeLine(each);
    }

    // then output is correct
    assertThat(
        out,
        allOf(
            hasItemAt(0, equalTo("Hello")),
            hasItemAt(1, equalTo("World"))
        )
    );
    assertEquals(2, out.size());
  }
}
