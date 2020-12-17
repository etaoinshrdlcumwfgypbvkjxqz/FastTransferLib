package dev.technici4n.fasttransferlib.experimental.impl.content;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class AbstractContent<T>
        implements Content {
    @Override
    @NotNull
    public abstract Class<T> getCategory();

    @Override
    @NotNull
    public abstract T getType();

    @Override
    @NotNull
    public abstract Object getData();

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content)) return false;
        Content that = (Content) o;
        return getType().equals(that.getType()) && getData().equals(that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getData());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("type=" + getType())
                .add("data=" + getData())
                .toString();
    }
}
