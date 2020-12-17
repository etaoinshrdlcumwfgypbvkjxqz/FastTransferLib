package dev.technici4n.fasttransferlib.experimental.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

import java.util.Objects;
import java.util.StringJoiner;

public final class FluidContent
        implements Content {
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

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content)) return false;
        Content that = (Content) o;
        return getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FluidContent.class.getSimpleName() + "[", "]")
                .add("type=" + getType())
                .add("data=" + getData())
                .toString();
    }
}
