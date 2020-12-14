package dev.technici4n.fasttransferlib.experimental.transfer.impl.context;

import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;

public class SimulationContext
        implements Context {
    private static final SimulationContext INSTANCE = new SimulationContext();

    public static SimulationContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void execute(Runnable action, Runnable rollback) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }
}
