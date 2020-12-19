package dev.technici4n.fasttransferlib.experimental.impl.content;

import dev.technici4n.fasttransferlib.experimental.api.content.ContentApi;
import net.minecraft.util.registry.Registry;

public enum ContentInit {
    ;

    static {
        ContentApi.DESERIALIZERS.put(ContentApi.EMPTY_KEY, EmptyContent::deserialize);
        ContentApi.DESERIALIZERS.put(Registry.ITEM_KEY.getValue(), ItemContent::deserialize);
        ContentApi.DESERIALIZERS.put(Registry.FLUID_KEY.getValue(), FluidContent::deserialize);
        ContentApi.DESERIALIZERS.put(ContentApi.ENERGY_KEY, EnergyContent::deserialize);
    }

    public static void initializeClass() {}
}
