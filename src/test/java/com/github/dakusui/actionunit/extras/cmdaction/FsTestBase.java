package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.n.actions.cmd.Commander;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.ActionSupport;
import com.github.dakusui.actionunit.n.core.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import static com.github.dakusui.actionunit.n.core.ActionSupport.*;

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
    return sequential(
        preparation(),
        target
    );
  }

  protected Action preparation() {
    return ActionSupport.nop();
  }

  private Function<Context, Action> withCleanUp(Action action) {
    return (c) -> attempt(action).recover(
        Exception.class,
        simple("rethrow", context -> {
          Throwable t = context.thrownException().orElseThrow(RuntimeException::new);
          if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
          } else if (t instanceof Error) {
            throw (Error) t;
          }
          throw new RuntimeException(t);
        }))
        .ensure(cleanUp());
  }

  private Action cleanUp() {
    ////
    // This is necessary because 'deleteOnExit' only requests to remove files
    // created inside JVM.
    return new Commander() {
      @Override
      protected String program() {
        return "/bin/rm";
      }
    }.add("-rf").add(dir.getAbsolutePath()).build();
  }

}
