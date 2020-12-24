package dev.technici4n.fasttransferlib.impl.context;

import dev.technici4n.fasttransferlib.api.Context;

public class SimulationContext
        implements Context {
    private static final SimulationContext INSTANCE = new SimulationContext();

    public static SimulationContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void configure(Runnable action, Runnable rollback) {
        // NOOP
    }

    @Override
    public void execute(Runnable action) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}