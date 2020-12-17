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

import java.util.Objects;
import java.util.StringJoiner;

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
    public Item getType() {
        return type;
    }

    @Override
    @NotNull
    public CompoundTag getData() {
        return data.copy();
    }

    @Override
    public Class<?> getCategory() {
        return Item.class;
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
        return getType().equals(that.getType()) && getData().equals(that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getData());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ItemContent.class.getSimpleName() + "[", "]")
                .add("type=" + getType())
                .add("data=" + getData())
                .toString();
    }
}
