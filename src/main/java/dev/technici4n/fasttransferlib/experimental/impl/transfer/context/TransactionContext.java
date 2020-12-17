package dev.technici4n.fasttransferlib.experimental.impl.transfer.context;

import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionContext
        extends ExecutionContext {
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
