package dev.technici4n.fasttransferlib.experimental.transfer.impl.context;

import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;

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
