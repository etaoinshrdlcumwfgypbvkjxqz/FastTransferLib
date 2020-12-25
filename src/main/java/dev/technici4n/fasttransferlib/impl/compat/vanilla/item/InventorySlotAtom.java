package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.OptionalLong;

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

    @Override
    public OptionalLong getCapacity() {
        Inventory inventory = getInventory();
        ItemStack stack = inventory.getStack(getSlot());
        return OptionalLong.of(stack.isEmpty()
                ? inventory.getMaxCountPerStack()
                : Math.min(inventory.getMaxCountPerStack(), stack.getMaxCount()));
    }

    protected Inventory getInventory() {
        return inventory;
    }

    protected int getSlot() {
        return slot;
    }

    @Override
    protected long extractCurrent(Context context, long maxAmount) {
        Inventory inventory = getInventory();
        ItemStack stack = inventory.getStack(getSlot());
        assert !stack.isEmpty();

        // stack is not empty, item matches, can extract
        int amount = Math.toIntExact(Math.min(maxAmount, stack.getCount())); // COMMENT should be in int range, negative excluded
        context.configure(() -> stack.decrement(amount), () -> stack.increment(amount));
        context.execute(inventory::markDirty);

        return amount;
    }

    @Override
    protected long insertCurrent(Context context, long maxAmount) {
        Inventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);

        int maxCount = Math.min(stack.getMaxCount(), inventory.getMaxCountPerStack());

        int amount = Math.toIntExact(Math.min(maxAmount, maxCount - stack.getCount()));
        context.configure(() -> stack.increment(amount), () -> stack.decrement(amount));
        context.execute(inventory::markDirty);

        return maxAmount - amount;
    }

    @Override
    protected long insertNew(Context context, Content content, Item type, long maxAmount) {
        Inventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);
        assert stack.isEmpty();
        int maxCount = Math.min(type.getMaxCount(), inventory.getMaxCountPerStack());

        int amount = Math.toIntExact(Math.min(maxAmount, maxCount));
        context.configure(
                () -> inventory.setStack(slot, ItemContent.asStack(content, amount)),
                () -> inventory.setStack(slot, stack)
        );
        context.execute(inventory::markDirty);

        return maxAmount - amount;
    }

    @Override
    protected boolean supportsPushNotification() {
        return false; // inventory sucks
    }

    @Override
    protected boolean supportsPullNotification() {
        return false; // inventory sucks
    }
}
