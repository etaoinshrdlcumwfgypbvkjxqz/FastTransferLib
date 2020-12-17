package dev.technici4n.fasttransferlib.experimental.impl.content;

import dev.technici4n.fasttransferlib.experimental.api.Content;

import java.util.StringJoiner;

public enum EmptyContent
        implements Content {
    INSTANCE;

    @Override
    public Object getType() {
        return null;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public Class<?> getCategory() {
        return Void.class;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EmptyContent.class.getSimpleName() + "[", "]")
                .toString();
    }
}
