package dev.technici4n.fasttransferlib.impl.util;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.function.BiConsumer;

public enum ViewImplUtilities {
    ;

    public static <T> BiConsumer<Object2LongMap<T>, Object2LongMap<T>> getQuantityMapsMerger() {
        return (container1, container2) -> {
            for (Object2LongMap.Entry<T> e : container2.object2LongEntrySet())
                container1.mergeLong(e.getKey(), e.getLongValue(), Long::sum);
        };
    }
}
