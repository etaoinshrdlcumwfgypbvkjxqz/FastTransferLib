package dev.technici4n.fasttransferlib.experimental.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemContent
        extends AbstractContent<Item> {
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

    public static ItemContent of(Item type, @Nullable CompoundTag data) {
        if (data == null || NO_DATA.equals(data))
            return of(type);
        return new ItemContent(type, data);
    }

    public static ItemContent of(Item type) {
        return CACHE.getUnchecked(type);
    }

    public static Content of(ItemStack stack) {
        return stack.isEmpty() ? EmptyContent.INSTANCE : of(stack.getItem(), stack.getTag());
    }

    @Override
    public @NotNull Item getType() {
        return type;
    }

    @Override
    @NotNull
    public CompoundTag getData() {
        return data.copy();
    }

    @Override
    public @NotNull Class<Item> getCategory() {
        return Item.class;
    }
}
