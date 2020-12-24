package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
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
            context.configure(
                    () -> inventory.setStack(slot, ItemContent.asStack(content, amount)),
                    () -> inventory.setStack(slot, stack)
            );
            context.execute(inventory::markDirty);
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
        Inventory inventory = getInventory();
        ItemStack stack = inventory.getStack(getSlot());
        if (!stack.isEmpty() && content.equals(ItemContent.of(stack))) {
            // stack is not empty, item matches, can extract
            int amount = Math.toIntExact(Math.min(maxAmount, stack.getCount())); // COMMENT should be in int range, negative excluded
            context.configure(() -> stack.decrement(amount), () -> stack.increment(amount));
            context.execute(inventory::markDirty);
            return amount;
        }
        return 0L;
    }
}
