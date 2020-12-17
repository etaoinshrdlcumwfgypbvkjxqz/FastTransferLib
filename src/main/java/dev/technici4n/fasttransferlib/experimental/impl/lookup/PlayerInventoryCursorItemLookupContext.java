package dev.technici4n.fasttransferlib.experimental.impl.lookup;

import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class PlayerInventoryCursorItemLookupContext
        implements ItemLookupContext {
    private final PlayerEntity player;
    private final Content itemContent;

    protected PlayerInventoryCursorItemLookupContext(PlayerEntity player) {
        this.player = player;
        this.itemContent = ItemContent.of(player.inventory.getCursorStack());
    }

    public static PlayerInventoryCursorItemLookupContext of(PlayerEntity player) {
        return new PlayerInventoryCursorItemLookupContext(player);
    }

    @Override
    public boolean set(Context context, Content content, long count) {
        PlayerEntity player = getPlayer();
        ItemStack previousStack = player.inventory.getCursorStack();
        context.execute(() -> player.inventory.setCursorStack(new ItemStack((Item) content.getType(), Ints.saturatedCast(count))),
                () -> player.inventory.setCursorStack(previousStack));
        return true;
    }

    @Override
    public long getCount() {
        ItemStack cursorItemStack = getPlayer().inventory.getCursorStack();
        return getItemContent().equals(ItemContent.of(cursorItemStack)) ? cursorItemStack.getCount() : 0;
    }

    @Override
    public CompoundTag getTag() {
        ItemStack handItemStack = getPlayer().inventory.getCursorStack();
        return getItemContent().equals(ItemContent.of(handItemStack)) ? handItemStack.getTag() : new CompoundTag();
    }

    protected PlayerEntity getPlayer() {
        return player;
    }

    protected Content getItemContent() {
        return itemContent;
    }
}
