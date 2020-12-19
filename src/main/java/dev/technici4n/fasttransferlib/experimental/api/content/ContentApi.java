package dev.technici4n.fasttransferlib.experimental.api.content;

import com.google.common.collect.MapMaker;
import dev.technici4n.fasttransferlib.experimental.impl.ApiInit;
import dev.technici4n.fasttransferlib.experimental.impl.content.EmptyContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public enum ContentApi {
    ;

    // TODO better solution
    public static final ConcurrentMap<Identifier, Function<? super CompoundTag, ? extends Content>> DESERIALIZERS =
            new MapMaker().concurrencyLevel(1).initialCapacity(16).makeMap();
    public static final ConcurrentMap<Identifier, EnergyType> ENERGY_DESERIALIZERS =
            new MapMaker().concurrencyLevel(1).initialCapacity(16).makeMap();

    public static final Identifier EMPTY_KEY = new Identifier(ApiInit.MOD_ID, "empty");
    public static final Identifier ENERGY_KEY = new Identifier(ApiInit.MOD_ID, "energy");

    static {
        ApiInit.initializeClass();
    }

    public static CompoundTag serialize(Content content) {
        CompoundTag result = new CompoundTag();
        result.putString("identifier", content.getIdentifier().toString());
        result.put("content", content.serialize());
        return result;
    }

    public static Content deserialize(CompoundTag serialized) {
        if (serialized.contains("identifier") && serialized.contains("content"))
            DESERIALIZERS.getOrDefault(new Identifier(serialized.getString("identifier")),
                    tag -> EmptyContent.INSTANCE)
                    .apply(serialized.getCompound("content"));
        return EmptyContent.INSTANCE;
    }
}
