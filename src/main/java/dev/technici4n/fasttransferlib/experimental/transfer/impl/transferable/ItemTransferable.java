package dev.technici4n.fasttransferlib.experimental.transfer.impl.transferable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Transferable;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

public final class ItemTransferable
        implements Transferable {
    private static final CompoundTag NO_DATA = new CompoundTag();
    private static final LoadingCache<Item, ItemTransferable> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(key -> new ItemTransferable(key, NO_DATA)));
    private final Item type;
    private final CompoundTag data;

    private ItemTransferable(Item type, CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    public static ItemTransferable of(Item type, CompoundTag data) {
        return new ItemTransferable(type, data);
    }

    public static ItemTransferable of(Item type) {
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
