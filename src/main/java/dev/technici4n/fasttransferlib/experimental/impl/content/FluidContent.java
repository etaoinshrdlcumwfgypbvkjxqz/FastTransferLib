package dev.technici4n.fasttransferlib.experimental.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import org.jetbrains.annotations.NotNull;

public final class FluidContent
        extends AbstractContent<Fluid> {
    private static final Object NO_DATA = new Object();
    private static final LoadingCache<Fluid, FluidContent> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(FluidContent::new));
    private final Fluid type;

    private FluidContent(Fluid type) {
        this.type = type;
    }

    public static Content of(Fluid type) {
        return type == Fluids.EMPTY ? EmptyContent.INSTANCE : CACHE.getUnchecked(type);
    }

    @Override
    public @NotNull Fluid getType() {
        return type;
    }

    @Override
    public @NotNull Object getData() {
        return NO_DATA;
    }

    @Override
    public @NotNull Class<Fluid> getCategory() {
        return Fluid.class;
    }
}
