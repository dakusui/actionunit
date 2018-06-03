package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

public abstract class FsTestBase<C extends Commander<C>> extends CommanderTestBase<C> {
  protected final File dir;

  public FsTestBase() throws IOException {
    super();
    this.dir = Files.createTempDirectory("tmp").toFile();
    this.dir.deleteOnExit();
    this.commander.cwd(this.dir);
  }

  @Override
  public void perform(Action action) {
    super.perform(withPreparation(withCleanUp(action).apply(this.context), context));
  }

  private Action withPreparation(Action target, Context context) {
    return context.sequential(
        preparation().apply(context),
        target
    );
  }

  protected Function<Context, Action> preparation() {
    return Context::nop;
  }

  private Function<Context, Action> withCleanUp(Action action) {
    return (c) -> c.attempt(action).recover(
        Exception.class,
        (factory, data) ->
            factory.simple("rethrow", () -> {
              Throwable t = data.get();
              if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
              } else if (t instanceof Error) {
                throw (Error) t;
              }
              throw new RuntimeException(t);
            }))
        .ensure(this::cleanUp);
  }

  private Action cleanUp(Context context) {
    ////
    // This is necessary because 'deleteOnExit' only requests to remove files
    // created inside JVM.
    return new Commander(context) {
      @Override
      protected String program() {
        return "/bin/rm";
      }
    }.add("-rf").add(dir.getAbsolutePath()).build();
  }

}
