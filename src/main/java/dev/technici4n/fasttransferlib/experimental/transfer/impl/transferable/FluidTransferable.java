package dev.technici4n.fasttransferlib.experimental.transfer.impl.transferable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Transferable;
import net.minecraft.fluid.Fluid;

public final class FluidTransferable
        implements Transferable {
    private static final Object NO_DATA = new Object();
    private static final LoadingCache<Fluid, FluidTransferable> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(FluidTransferable::new));
    private final Fluid type;

    private FluidTransferable(Fluid type) {
        this.type = type;
    }

    public static FluidTransferable of(Fluid type) {
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
