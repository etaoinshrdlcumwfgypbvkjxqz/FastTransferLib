package dev.technici4n.fasttransferlib.experimental.impl.context;

import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.experimental.api.Context;

import java.util.ArrayDeque;
import java.util.Deque;

public class TransactionContext
        extends ExecutionContext {
    private final Deque<Runnable> rollbackActions;
    private boolean rollback = true;

    public TransactionContext(long estimatedActions) {
        this.rollbackActions = new ArrayDeque<>(Ints.saturatedCast(estimatedActions));
    }

    @Override
    public void execute(Runnable action, Runnable rollback) {
        super.execute(action, rollback); // execute now
        getRollbackActions().push(rollback);
    }

    public void commitWith(Context context) {
        context.execute(this::commit, this::rollback);
    }

    public void commit() {
        setRollback(false);
    }

    public void rollback() {
        // first is head
        getRollbackActions().forEach(Runnable::run);
    }

    @Override
    public void close() {
        if (isRollback())
            rollback();
    }

    protected boolean isRollback() {
        return rollback;
    }

    protected void setRollback(@SuppressWarnings("SameParameterValue") boolean rollback) {
        this.rollback = rollback;
    }

    protected Deque<Runnable> getRollbackActions() {
        return rollbackActions;
    }
}
