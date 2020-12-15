package dev.technici4n.fasttransferlib.experimental.impl.compat.item;

import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.experimental.api.Instance;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.participant.SingleCategoryParticipant;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

public class SidedInventoryParticipant
		extends SingleCategoryParticipant<Item> {
	private final Inventory wrapped;
	private final SidedInventory wrappedSided;
	private final int[] slots;
	private final int size;
	private final Direction direction;

	public SidedInventoryParticipant(Inventory wrapped, Direction direction) {
		super(Item.class);
		this.wrapped = wrapped;
		this.wrappedSided = wrapped instanceof SidedInventory ? (SidedInventory) wrapped : null;
		this.slots = wrappedSided != null ? wrappedSided.getAvailableSlots(direction) : null;
		this.size = slots == null ? wrapped.size() : slots.length;
		this.direction = direction;
	}

	@Override
	protected long insert(Context context, Instance instance, Item type, long maxAmount) {
		int size = this.size;
		int[] slots = this.slots;
		boolean sided = wrappedSided != null;
		Inventory wrapped = this.wrapped;
		SidedInventory wrappedSided = this.wrappedSided;

		int maxCount = Math.min(wrapped.getMaxCountPerStack(), type.getMaxCount());
		Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
				Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
				Util.identityHashStrategy()
		);

		for (int index = 0; index < size; ++index) {
			int slot = sided ? slots[index] : index;
			ItemStack stack = wrapped.getStack(slot);
			if ((stack.isEmpty() || stack.getItem() == type) && (!sided || wrappedSided.canInsert(index, stack, direction))) {
				// stack is empty or its item matches, can insert
				int amount;  // COMMENT should be in int range, negative excluded
				if (stack.isEmpty()) {
					amount = Math.toIntExact(Math.min(maxAmount, maxCount));
					ItemStack previousStack = stack;
					ItemStack nextStack = stack = new ItemStack(type, 0);
					context.execute(() -> {
						wrapped.setStack(slot, nextStack);
						wrapped.markDirty();
					}, () -> {
						wrapped.setStack(slot, previousStack);
						wrapped.markDirty();
					});
				} else {
					amount = Math.toIntExact(Math.min(maxAmount, maxCount - stack.getCount()));
				}
				incrementalActions.put(stack, amount);
				maxAmount -= amount;
				assert maxAmount >= 0L;
				if (maxAmount == 0L)
					break;
			}
		}

		context.execute(() -> {
			incrementalActions.forEach(ItemStack::increment);
			if (!incrementalActions.isEmpty())
				wrapped.markDirty();
		}, () -> {
			incrementalActions.forEach(ItemStack::decrement);
			if (!incrementalActions.isEmpty())
				wrapped.markDirty();
		});

		return maxAmount;
	}

	@Override
	protected long extract(Context context, Instance instance, Item type, long maxAmount) {
		long leftoverAmount = maxAmount;

		int size = this.size;
		int[] slots = this.slots;
		boolean sided = wrappedSided != null;
		Inventory wrapped = this.wrapped;
		SidedInventory wrappedSided = this.wrappedSided;

		int maxCount = Math.min(wrapped.getMaxCountPerStack(), type.getMaxCount());
		Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
				Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
				Util.identityHashStrategy()
		);

		for (int index = 0; index < size; ++index) {
			ItemStack stack = wrapped.getStack(sided ? slots[index] : index);
			if (!stack.isEmpty() && stack.getItem() == type && (!sided || wrappedSided.canExtract(index, stack, direction))) {
				// stack is not empty, item matches, can extract
				int amount = Math.toIntExact(Math.min(leftoverAmount, stack.getCount())); // COMMENT should be in int range, negative excluded
				incrementalActions.put(stack, amount);
				leftoverAmount -= amount;
				assert leftoverAmount >= 0L;
				if (leftoverAmount == 0L)
					break;
			}
		}

		context.execute(() -> {
			incrementalActions.forEach(ItemStack::decrement);
			if (!incrementalActions.isEmpty())
				wrapped.markDirty();
		}, () -> {
			incrementalActions.forEach(ItemStack::increment);
			if (!incrementalActions.isEmpty())
				wrapped.markDirty();
		});

		return maxAmount - leftoverAmount;
	}
}
