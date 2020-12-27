package dev.technici4n.fasttransferlib.impl.context;

import dev.technici4n.fasttransferlib.api.context.StatelessContext;

public class ExecutionContext
        implements StatelessContext {
    private static final ExecutionContext INSTANCE = new ExecutionContext();

    public static ExecutionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void configure(Runnable action, Runnable reaction) {
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
