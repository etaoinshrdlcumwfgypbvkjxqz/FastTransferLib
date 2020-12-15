package dev.technici4n.fasttransferlib.experimental.impl.instance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.Instance;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

public final class ItemInstance
        implements Instance {
    private static final CompoundTag NO_DATA = new CompoundTag();
    private static final LoadingCache<Item, ItemInstance> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(key -> new ItemInstance(key, NO_DATA)));
    private final Item type;
    private final CompoundTag data;

    private ItemInstance(Item type, CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    public static ItemInstance of(Item type, CompoundTag data) {
        return new ItemInstance(type, data);
    }

    public static ItemInstance of(Item type) {
        return CACHE.getUnchecked(type);
    }

    @Override
    public Item getType() {
        return type;
    }

    @Override
    public CompoundTag getData() {
        return data.copy();
    }

    @Override
    public Class<?> getCategory() {
        return Item.class;
    }
}
