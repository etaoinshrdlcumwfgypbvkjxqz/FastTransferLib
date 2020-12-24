package dev.technici4n.fasttransferlib.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.ContentApi;
import dev.technici4n.fasttransferlib.api.content.EnergyType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public final class EnergyContent
        extends AbstractContent<EnergyType> {
    private static final LoadingCache<EnergyType, EnergyContent> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(EnergyContent::new));

    private EnergyContent(EnergyType type) {
        super(type);
    }

    public static EnergyContent of(EnergyType type) {
        return CACHE.getUnchecked(type);
    }

    @Override
    public @NotNull Class<EnergyType> getCategory() {
        return EnergyType.class;
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
    public Identifier getIdentifier() {
        return ContentApi.ENERGY_KEY;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag result = new CompoundTag();
        result.putString("type", getType().getIdentifier().toString());
        return result;
    }

    public static Content deserialize(CompoundTag serialized) {
        if (serialized.contains("type")) {
            EnergyType type = ContentApi.ENERGY_DESERIALIZERS.get(new Identifier(serialized.getString("type")));
            if (type != null)
                return EnergyContent.of(type);
        }
        return EmptyContent.INSTANCE;
    }
}
