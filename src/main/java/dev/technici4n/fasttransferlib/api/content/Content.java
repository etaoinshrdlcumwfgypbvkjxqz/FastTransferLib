package dev.technici4n.fasttransferlib.api.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface Content {
    @Nullable
    Object getType();

    @Nullable
    Object getData();

    Class<?> getCategory();

    boolean isEmpty();

    boolean equals(Object obj);

    CompoundTag serialize();

    Identifier getIdentifier();
}
