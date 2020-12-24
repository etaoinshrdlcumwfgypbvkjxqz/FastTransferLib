package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class SidedInventorySlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final SidedInventory inventory;
    private final Direction direction;
    private final int slot;

    public SidedInventorySlotAtom(SidedInventory inventory, Direction direction, int slot) {
        super(Item.class);
        this.inventory = inventory;
        this.direction = direction;
        this.slot = slot;
    }

    @Override
    public Content getContent() {
        return ItemContent.of(getInventory().getStack(getSlot()));
    }

    @Override
    public long getAmount() {
        return getInventory().getStack(getSlot()).getCount();
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        SidedInventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);

        int maxCount = Math.min(type.getMaxCount(), inventory.getMaxCountPerStack());

        int amount;
        if (stack.isEmpty())  {
            int amount1 = Math.toIntExact(Math.min(maxAmount, maxCount));
            ItemStack nextStack = ItemContent.asStack(content, amount1);
            if (inventory.canInsert(slot, nextStack, getDirection())) {
                amount = amount1;
                nextStack.setCount(amount);
                context.configure(() -> inventory.setStack(slot, nextStack), () -> inventory.setStack(slot, stack));
                context.execute(inventory::markDirty);
            } else
                amount = 0;
        } else if (content.equals(ItemContent.of(stack))) {
            amount = Math.toIntExact(Math.min(maxAmount, maxCount - stack.getCount()));
            context.configure(() -> stack.increment(amount), () -> stack.decrement(amount));
            context.execute(inventory::markDirty);
        } else
            amount = 0;

        return maxAmount - amount;
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        SidedInventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);

        if (!stack.isEmpty() && content.equals(ItemContent.of(stack)) && inventory.canExtract(slot, stack, getDirection())) {
            // stack is not empty, item matches, can extract
            int amount = Math.toIntExact(Math.min(maxAmount, stack.getCount())); // COMMENT should be in int range, negative excluded
            context.configure(() -> stack.decrement(amount), () -> stack.increment(amount));
            context.execute(inventory::markDirty);
            return amount;
        }
        return 0L;
    }

    protected SidedInventory getInventory() {
        return inventory;
    }

    protected Direction getDirection() {
        return direction;
    }

    protected int getSlot() {
        return slot;
    }
}
