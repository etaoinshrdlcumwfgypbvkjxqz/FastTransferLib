package dev.technici4n.fasttransferlib.impl.content;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.api.content.Content;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemContent
        extends AbstractContent<Item> {
    private static final CompoundTag NO_DATA = new CompoundTag();
    private static final LoadingCache<Item, ItemContent> CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .initialCapacity(16)
            .build(CacheLoader.from(key -> new ItemContent(key, NO_DATA)));
    private final CompoundTag internalData;

    private ItemContent(Item type, CompoundTag data) {
        super(type);
        this.internalData = data.copy();
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

    public static ItemStack asStack(Content content, int quantity) {
        if (content.getCategory() != Item.class)
            throw new IllegalArgumentException();
        ItemStack result = new ItemStack((Item) content.getType(), quantity);
        result.setTag((CompoundTag) content.getData());
        return result;
    }

    @Override
    @NotNull
    public CompoundTag getData() {
        return getInternalData().copy();
    }

    @Override
    protected CompoundTag getInternalData() {
        return internalData;
    }

    @Override
    public @NotNull Class<Item> getCategory() {
        return Item.class;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag result = new CompoundTag();
        result.putString("type", Registry.ITEM.getId(getType()).toString());
        result.put("data", getInternalData().copy());
        return result;
    }

    public static Content deserialize(CompoundTag serialized) {
        if (serialized.contains("type")) {
            Item type = Registry.ITEM.get(new Identifier(serialized.getString("type")));
            CompoundTag data = serialized.contains("data") ? serialized.getCompound("data") : null;
            return ItemContent.of(type, data);
        }
        return EmptyContent.INSTANCE;
    }

    @Override
    public Identifier getIdentifier() {
        return Registry.ITEM_KEY.getValue();
    }
}
