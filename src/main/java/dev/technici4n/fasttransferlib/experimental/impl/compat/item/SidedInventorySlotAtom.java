package dev.technici4n.fasttransferlib.experimental.impl.compat.item;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.experimental.impl.view.AbstractMonoCategoryAtom;
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
            ItemStack nextStack = new ItemStack(type);
            if (inventory.canInsert(slot, nextStack, getDirection())) {
                amount = Math.toIntExact(Math.min(maxAmount, maxCount));
                nextStack.setCount(amount);
                context.execute(() -> {
                    inventory.setStack(slot, nextStack);
                    inventory.markDirty();
                }, () -> {
                    inventory.setStack(slot, stack);
                    inventory.markDirty();
                });
            } else
                amount = 0;
        } else if (content.equals(ItemContent.of(stack))) {
            amount = Math.toIntExact(Math.min(maxAmount, maxCount - stack.getCount()));
            context.execute(() -> {
                stack.increment(amount);
                inventory.markDirty();
            }, () -> {
                stack.decrement(amount);
                inventory.markDirty();
            });
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
            context.execute(() -> {
                stack.decrement(amount);
                inventory.markDirty();
            }, () -> {
                stack.increment(amount);
                inventory.markDirty();
            });
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
