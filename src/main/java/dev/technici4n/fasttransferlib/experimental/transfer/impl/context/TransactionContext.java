package dev.technici4n.fasttransferlib.experimental.transfer.impl.context;

import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;

import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionContext
        extends ExecutionContext
        implements Context {
    private final Deque<Runnable> rollbackActions;

    public TransactionContext(int initialCapacity) {
        this.rollbackActions = new ArrayDeque<>(initialCapacity);
    }

    @Override
    public void execute(Runnable action, Runnable rollback) {
        super.execute(action, rollback);
        rollbackActions.push(rollback);
    }

    public void commit() {
        rollbackActions.clear();
    }

    @Override
    public void close() {
        // first is head
        rollbackActions.forEach(Runnable::run);
    }
}
