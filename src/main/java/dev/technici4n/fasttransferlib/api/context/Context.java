package dev.technici4n.fasttransferlib.api.context;

public interface Context
        extends AutoCloseable {
    // Newton's 3rd Law - Every *action* has a *reaction* of equal magnitude but opposite direction.

    void configure(Runnable action, Runnable reaction);

    void execute(Runnable action);

    @Override
    void close();
}
