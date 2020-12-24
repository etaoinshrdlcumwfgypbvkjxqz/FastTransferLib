package dev.technici4n.fasttransferlib.impl.util;

public interface UncheckedAutoCloseable
        extends AutoCloseable {
    @Override
    void close();
}
