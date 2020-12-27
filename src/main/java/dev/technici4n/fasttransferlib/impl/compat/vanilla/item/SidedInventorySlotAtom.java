package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.event.PullEvent;
import dev.technici4n.fasttransferlib.api.view.event.PushEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferNetEvent;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class SidedInventorySlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private static final Set<Class<? extends PullEvent>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferNetEvent.class);
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
    public long getQuantity() {
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

    protected SidedInventory getInventory() {
        return inventory;
    }

    protected Direction getDirection() {
        return direction;
    }

    protected int getSlot() {
        return slot;
    }

    @Override
    protected Collection<? extends Class<? extends PushEvent>> getSupportedPushEvents() {
        return ImmutableSet.of(); // inventory sucks
    }

    @Override
    protected Collection<? extends Class<? extends PullEvent>> getSupportedPullEvents() {
        return SUPPORTED_PULL_EVENTS; // inventory sucks
    }

    @Override
    public Object getRevisionFor(Class<? extends PullEvent> event) {
        if (event == TransferNetEvent.class)
            return getInventory().getStack(getSlot()).copy(); // need to copy to avoid being modified
        return super.getRevisionFor(event);
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        SidedInventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);
        assert !stack.isEmpty();

        if (inventory.canExtract(slot, stack, getDirection())) {
            // stack is not empty, item matches, can extract
            int quantity = Math.toIntExact(Math.min(maxQuantity, stack.getCount())); // COMMENT should be in int range, negative excluded
            context.configure(() -> stack.decrement(quantity), () -> stack.increment(quantity));
            context.execute(inventory::markDirty);
            return quantity;
        }

        return 0L;
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        SidedInventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);

        int maxCount = Math.min(stack.getMaxCount(), inventory.getMaxCountPerStack());

        int quantity = Math.toIntExact(Math.min(maxQuantity, maxCount - stack.getCount()));
        context.configure(() -> stack.increment(quantity), () -> stack.decrement(quantity));
        context.execute(inventory::markDirty);

        return maxQuantity - quantity;
    }

    @Override
    protected long insertNew(Context context, Content content, Item type, long maxQuantity) {
        SidedInventory inventory = getInventory();
        int slot = getSlot();
        ItemStack stack = inventory.getStack(slot);

        int maxCount = Math.min(type.getMaxCount(), inventory.getMaxCountPerStack());

        int quantity = Math.toIntExact(Math.min(maxQuantity, maxCount));
        ItemStack nextStack = ItemContent.asStack(content, quantity);
        if (inventory.canInsert(slot, nextStack, getDirection())) {
            nextStack.setCount(quantity);
            context.configure(() -> inventory.setStack(slot, nextStack), () -> inventory.setStack(slot, stack));
            context.execute(inventory::markDirty);
        } else
            quantity = 0;

        return maxQuantity - quantity;
    }

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
            if (query instanceof TransferQuery)
                return TriState.TRUE;
            if (query instanceof StoreQuery)
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
    }
}
