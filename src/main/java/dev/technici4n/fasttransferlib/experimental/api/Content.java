package dev.technici4n.fasttransferlib.experimental.api;

import org.jetbrains.annotations.Nullable;

public interface Content {
    @Nullable
    Object getType();

    @Nullable
    Object getData();

    Class<?> getCategory();

    boolean isEmpty();

    boolean equals(Object obj);
}
