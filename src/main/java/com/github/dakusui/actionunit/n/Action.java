package com.github.dakusui.actionunit.n;

import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.stream.Stream;

public interface Action {

  void accept(Visitor visitor);

  interface Visitor {
    @SuppressWarnings("unused")
    default void visit(Action action) {
      throw new UnsupportedOperationException();
    }

    default void visit(Leaf action) {
      this.visit((Action) action);
    }

    default void visit(Named action) {
      this.visit((Action) action);
    }

    default void visit(Composite action) {
      this.visit((Action) action);
    }

    default <E> void visit(ForEach<E> action) {
      this.visit((Action) action);
    }

    default void visit(Attempt action) {
      this.visit((Action) action);
    }

    class Performer implements Visitor {
      private Context context;

      Performer() {
        this(Context.create());
      }

      private Performer(Context context) {
        this.context = context;
      }

      @Override
      public void visit(Leaf action) {
        action.runnable(context).run();
      }

      @Override
      public void visit(Composite action) {
        Stream<Action> actionStream = action.isParallel()
            ? action.children().parallelStream()
            : action.children().stream();
        actionStream.forEach(
            a -> a.accept(this)
        );
      }

      @Override
      public <E> void visit(ForEach<E> action) {
        Stream<E> data = action.isParallel()
            ? action.data().parallel()
            : action.data();
        data.forEach(
            e -> action.perform().accept(
                new Performer(
                    this.context.createChild().assignTo(
                        action.loopVariableName(),
                        e
                    ))));
      }

      @Override
      public void visit(Attempt action) {
        try {
          action.perform().accept(this);
        } catch (Throwable t) {
          if (action.targetExceptionClass().isAssignableFrom(t.getClass()))
            action.recover().accept(new Performer(this.context.createChild().assignTo(Context.Impl.ONGOING_EXCEPTION, t)));
          if (t instanceof Error)
            throw (Error) t;
          throw new ActionException(t);
        } finally {
          action.ensure().accept(this);
        }
      }

    }
  }

}
