package dev.technici4n.fasttransferlib.impl.context;

import dev.technici4n.fasttransferlib.api.Context;

public class ExecutionContext
        implements Context {
    private static final ExecutionContext INSTANCE = new ExecutionContext();

    public static ExecutionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void configure(Runnable action, Runnable rollback) {
        action.run();
    }

    @Override
    public void execute(Runnable action) {
        action.run();
    }

    @Override
    public void close() {
        // NOOP
    }
}
