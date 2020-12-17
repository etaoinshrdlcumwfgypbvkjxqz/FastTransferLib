package dev.technici4n.fasttransferlib.experimental.impl.lookup;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.lookup.BlockLookupContext;
import net.minecraft.util.math.Direction;

public class BlockLookupContextImpl
        implements BlockLookupContext {
    private static final LoadingCache<Direction, BlockLookupContextImpl> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(BlockLookupContextImpl::new));
    private final Direction direction;

    private BlockLookupContextImpl(Direction direction) {
        this.direction = direction;
    }

    public static BlockLookupContextImpl of(Direction direction) {
        return CACHE.getUnchecked(direction);
    }

    @Override
    public Direction getDirection() {
        return direction;
    }
}
