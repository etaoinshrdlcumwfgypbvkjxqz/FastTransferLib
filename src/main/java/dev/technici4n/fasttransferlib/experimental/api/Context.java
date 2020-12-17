package dev.technici4n.fasttransferlib.experimental.api;

public interface Context
        extends AutoCloseable {
    void execute(Runnable action, Runnable rollback);

    @Override
    void close();
}
