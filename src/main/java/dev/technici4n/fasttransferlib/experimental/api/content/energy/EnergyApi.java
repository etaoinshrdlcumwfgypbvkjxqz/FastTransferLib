package dev.technici4n.fasttransferlib.experimental.api.content.energy;

import com.google.common.collect.MapMaker;
import dev.technici4n.fasttransferlib.experimental.impl.ApiInit;
import net.minecraft.util.Identifier;

import java.util.concurrent.ConcurrentMap;

public enum EnergyApi {
    ;

    public static final ConcurrentMap<Identifier, EnergyType> DESERIALIZERS =
            new MapMaker().concurrencyLevel(1).initialCapacity(16).makeMap();

    static {
        ApiInit.initializeClass();
    }
}
