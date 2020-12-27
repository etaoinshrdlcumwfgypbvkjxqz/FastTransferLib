package dev.technici4n.fasttransferlib.impl.context;

import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.context.StatelessContext;

import java.util.ArrayList;
import java.util.List;

public class DelayedExecutionContext
        implements Context {
    private final List<Runnable> actions = new ArrayList<>(4); // todo need to determine
    private static final ThreadLocal<DelayedExecutionContext> INSTANCES = ThreadLocal.withInitial(DelayedExecutionContext::new);

    protected DelayedExecutionContext() {}

    public static DelayedExecutionContext getInstance() {
        return INSTANCES.get();
    }

    @Override
    public void configure(Runnable action, Runnable reaction) {
        getActions().add(action);
    }

    @Override
    public void execute(Runnable action) {
        getActions().add(action);
    }

    @Override
    public void close() {
        getActions().clear();
    }

    public void execute() {
        getActions().forEach(Runnable::run);
        getActions().clear();
    }

    public void executeWith(StatelessContext context) {
        context.execute(this::execute);
    }

    protected List<Runnable> getActions() {
        return actions;
    }
}
