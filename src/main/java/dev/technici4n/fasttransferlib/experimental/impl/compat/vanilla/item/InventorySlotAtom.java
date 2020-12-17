package dev.technici4n.fasttransferlib.experimental.impl.compat.vanilla.item;

import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import dev.technici4n.fasttransferlib.experimental.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventorySlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final Inventory inventory;
    private final int slot;

    public InventorySlotAtom(Inventory inventory, int slot) {
        super(Item.class);
        this.inventory = inventory;
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

    protected Inventory getInventory() {
        return inventory;
    }

    protected int getSlot() {
        return slot;
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        Inventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);

        int maxCount = Math.min(type.getMaxCount(), inventory.getMaxCountPerStack());

        int amount;
        if (stack.isEmpty())  {
            amount = Math.toIntExact(Math.min(maxAmount, maxCount));
            context.execute(() -> {
                inventory.setStack(slot, ItemContent.asStack(content, amount));
                inventory.markDirty();
            }, () -> {
                inventory.setStack(slot, stack);
                inventory.markDirty();
            });
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
        ItemStack stack = getInventory().getStack(getSlot());
        if (!stack.isEmpty() && content.equals(ItemContent.of(stack))) {
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
}
