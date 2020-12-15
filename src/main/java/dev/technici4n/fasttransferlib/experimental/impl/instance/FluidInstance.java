package dev.technici4n.fasttransferlib.experimental.impl.instance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.Instance;
import net.minecraft.fluid.Fluid;

public final class FluidInstance
        implements Instance {
    private static final Object NO_DATA = new Object();
    private static final LoadingCache<Fluid, FluidInstance> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(FluidInstance::new));
    private final Fluid type;

    private FluidInstance(Fluid type) {
        this.type = type;
    }

    public static FluidInstance of(Fluid type) {
        return CACHE.getUnchecked(type);
    }

    @Override
    public Fluid getType() {
        return type;
    }

    @Override
    public Object getData() {
        return NO_DATA;
    }

    @Override
    public Class<?> getCategory() {
        return Fluid.class;
    }
}
