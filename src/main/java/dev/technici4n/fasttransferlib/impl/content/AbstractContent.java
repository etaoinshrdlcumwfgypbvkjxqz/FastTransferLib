package dev.technici4n.fasttransferlib.impl.content;

import dev.technici4n.fasttransferlib.api.content.Content;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class AbstractContent<T>
        implements Content {
    private final T type;
    public static final Object NO_DATA = new Object();

    protected AbstractContent(T type) {
        this.type = type;
    }

    @Override
    @NotNull
    public abstract Class<T> getCategory();

    @Override
    @NotNull
    public final T getType() {
        return type;
    }

    @Override
    @NotNull
    public abstract Object getData();

    protected abstract Object getInternalData();

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content)) return false;
        Content that = (Content) o;

        Object thatData;
        if (that instanceof AbstractContent) {
            AbstractContent<?> that1 = (AbstractContent<?>) that;
            thatData = that1.getInternalData();
        } else thatData = that.getData();

        return getType().equals(that.getType()) && getInternalData().equals(thatData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getInternalData());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("type=" + getType())
                .add("data=" + getInternalData())
                .toString();
    }
}
