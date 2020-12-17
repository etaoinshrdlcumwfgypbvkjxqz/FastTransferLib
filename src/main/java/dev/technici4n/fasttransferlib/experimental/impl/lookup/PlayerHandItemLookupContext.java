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
import net.minecraft.util.Hand;

public class PlayerHandItemLookupContext
        implements ItemLookupContext {
    private final PlayerEntity player;
    private final Content itemContent;
    private final Hand hand;

    protected PlayerHandItemLookupContext(PlayerEntity player, Hand hand) {
        this.player = player;
        this.itemContent = ItemContent.of(player.getStackInHand(hand));
        this.hand = hand;
    }

    public static PlayerHandItemLookupContext of(PlayerEntity player, Hand hand) {
        return new PlayerHandItemLookupContext(player, hand);
    }

    @Override
    public long getCount() {
        ItemStack handItemStack = getPlayer().getStackInHand(getHand());
        return getItemContent().equals(ItemContent.of(handItemStack)) ? handItemStack.getCount() : 0L;
    }

    @Override
    public CompoundTag getTag() {
        ItemStack handItemStack = getPlayer().getStackInHand(getHand());
        return getItemContent().equals(ItemContent.of(handItemStack)) ? handItemStack.getTag() : new CompoundTag();
    }

    @Override
    public boolean set(Context context, Content content, long count) {
        PlayerEntity player = getPlayer();
        Hand hand = getHand();
        ItemStack previousStack = player.getStackInHand(hand);
        context.execute(() -> player.setStackInHand(hand, new ItemStack((Item) content.getType(), Ints.saturatedCast(count))),
                () -> player.setStackInHand(hand, previousStack));
        return true;
    }

    protected PlayerEntity getPlayer() {
        return player;
    }

    protected Content getItemContent() {
        return itemContent;
    }

    protected Hand getHand() {
        return hand;
    }
}
