package dev.technici4n.fasttransferlib.experimental.api;

public interface Context
        extends AutoCloseable {
    void configure(Runnable action, Runnable rollback);

    void execute(Runnable action);

    @Override
    void close();
}
