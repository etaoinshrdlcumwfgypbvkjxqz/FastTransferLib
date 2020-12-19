package dev.technici4n.fasttransferlib.experimental.impl.content;

import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import dev.technici4n.fasttransferlib.experimental.api.content.ContentApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.StringJoiner;

public enum EmptyContent
        implements Content {
    INSTANCE;

    @Override
    public Void getType() {
        return null;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public Class<Void> getCategory() {
        return Void.class;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public CompoundTag serialize() {
        return new CompoundTag();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .toString();
    }

    public static Content deserialize(@SuppressWarnings("unused") CompoundTag serialized) {
        return EmptyContent.INSTANCE;
    }

    @Override
    public Identifier getIdentifier() {
        return ContentApi.EMPTY_KEY;
    }
}
