package dev.technici4n.fasttransferlib.impl.context;

import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.context.Context;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TransactionContext
        implements Context {
    private final Deque<Runnable> reactions;
    private final List<Runnable> commitActions;
    private boolean committed = false;

    public TransactionContext(long estimatedActions) {
        // estimating number of actions is fruitless because implementations may add multiple actions
        // todo array deque or linked list
        int estimatedActions1 = Ints.saturatedCast(estimatedActions);
        this.reactions = new ArrayDeque<>(estimatedActions1);
        this.commitActions = new ArrayList<>(estimatedActions1);
    }

    @Override
    public void configure(Runnable action, Runnable reaction) {
        action.run();
        getReactions().push(reaction);
    }

    @Override
    public void execute(Runnable action) {
        getCommitActions().add(action);
    }

    public void commitWith(Context context) {
        context.configure(this::commitReversibly, this::rollback);
        context.execute(this::execute);
    }

    @Override
    public void close() {
        if (!isCommitted())
            rollback();
    }

    public void commit() {
        commitReversibly();
        execute();
    }

    public void rollback() {
        // first is head
        getReactions().forEach(Runnable::run);
    }

    protected void execute() {
        getCommitActions().forEach(Runnable::run);
    }

    protected void commitReversibly() {
        setCommitted(true);
    }

    protected boolean isCommitted() {
        return committed;
    }

    protected void setCommitted(@SuppressWarnings("SameParameterValue") boolean committed) {
        this.committed = committed;
    }

    protected Deque<Runnable> getReactions() {
        return reactions;
    }

    protected List<Runnable> getCommitActions() {
        return commitActions;
    }
}
