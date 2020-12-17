package dev.technici4n.fasttransferlib.experimental.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.content.Energy;
import org.jetbrains.annotations.NotNull;

public final class EnergyContent
        extends AbstractContent<Energy> {
    private static final LoadingCache<Energy, EnergyContent> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(EnergyContent::new));

    private EnergyContent(Energy type) {
        super(type);
    }

    public static EnergyContent of(Energy type) {
        return CACHE.getUnchecked(type);
    }

    @Override
    public @NotNull Class<Energy> getCategory() {
        return Energy.class;
    }

    @Override
    public @NotNull Object getData() {
        return AbstractContent.NO_DATA;
    }
}
