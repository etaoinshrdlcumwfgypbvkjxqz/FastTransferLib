package dev.technici4n.fasttransferlib.experimental.impl.transfer.context;

import dev.technici4n.fasttransferlib.experimental.api.Context;

public class ExecutionContext
        implements Context {
    private static final ExecutionContext INSTANCE = new ExecutionContext();

    public static ExecutionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void execute(Runnable action, Runnable rollback) {
        action.run();
    }

    @Override
    public void close() {
        // NOOP
    }
}
