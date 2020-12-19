package dev.technici4n.fasttransferlib.experimental.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public final class FluidContent
        extends AbstractContent<Fluid> {
    private static final LoadingCache<Fluid, FluidContent> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(FluidContent::new));

    private FluidContent(Fluid type) {
        super(type);
    }

    public static Content of(Fluid type) {
        return type == Fluids.EMPTY ? EmptyContent.INSTANCE : CACHE.getUnchecked(type);
    }

    @Override
    public @NotNull Object getData() {
        return getInternalData();
    }

    @Override
    protected Object getInternalData() {
        return AbstractContent.NO_DATA;
    }

    @Override
    public @NotNull Class<Fluid> getCategory() {
        return Fluid.class;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag result = new CompoundTag();
        result.putString("type", Registry.FLUID.getId(getType()).toString());
        return result;
    }

    public static Content deserialize(CompoundTag serialized) {
        if (serialized.contains("type")) {
            Fluid type = Registry.FLUID.get(new Identifier(serialized.getString("type")));
            return FluidContent.of(type);
        }
        return EmptyContent.INSTANCE;
    }

    @Override
    public Identifier getIdentifier() {
        return Registry.FLUID_KEY.getValue();
    }
}
