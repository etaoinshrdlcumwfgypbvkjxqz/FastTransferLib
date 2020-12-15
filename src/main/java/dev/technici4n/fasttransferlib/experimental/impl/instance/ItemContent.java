package dev.technici4n.fasttransferlib.experimental.impl.instance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

public final class ItemContent
        implements Content {
    private static final CompoundTag NO_DATA = new CompoundTag();
    private static final LoadingCache<Item, ItemContent> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(key -> new ItemContent(key, NO_DATA)));
    private final Item type;
    private final CompoundTag data;

    private ItemContent(Item type, CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    public static ItemContent of(Item type, CompoundTag data) {
        return new ItemContent(type, data);
    }

    public static ItemContent of(Item type) {
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
