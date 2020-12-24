package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class SidedInventoryViewParticipant
		extends AbstractMonoCategoryParticipant<Item>
		implements View, ListModel {
	private final SidedInventory delegate;
	private final Direction direction;
	private final int[] slots;
	private final Supplier<? extends List<? extends Atom>> atomList;

	@SuppressWarnings("UnstableApiUsage")
	protected SidedInventoryViewParticipant(SidedInventory delegate, Direction direction) {
		super(Item.class);
		// SidedInventory violates the substitution principle, so do NOT extend InventoryViewParticipant
		this.delegate = delegate;
		this.direction = direction;
		this.slots = delegate.getAvailableSlots(direction);
		this.atomList = Suppliers.memoize(() -> {
			SidedInventory delegate1 = getDelegate();
			Direction direction1 = getDirection();
			return Arrays.stream(getSlots())
					.mapToObj(slot -> new SidedInventorySlotAtom(delegate1, direction1, slot))
					.collect(ImmutableList.toImmutableList());
		});
	}

	private static SidedInventoryViewParticipant of(SidedInventory delegate, Direction direction) {
		return new SidedInventoryViewParticipant(delegate, direction);
	}

	public static Participant ofParticipant(Inventory delegate, Direction direction) {
		if (delegate instanceof SidedInventory)
			return of((SidedInventory) delegate, direction);
		return InventoryViewParticipant.of(delegate);
	}

	public static View ofView(Inventory delegate, Direction direction) {
		if (delegate instanceof SidedInventory)
			return of((SidedInventory) delegate, direction);
		return InventoryViewParticipant.of(delegate);
	}

	@Override
	protected long insert(Context context, Content content, Item type, long maxAmount) {
		SidedInventory delegate = getDelegate();
		int[] slots = getSlots();
		int size = slots.length;
		Direction direction = getDirection();

		int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
		Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
				Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
				Util.identityHashStrategy()
		);

		for (int index = 0; index < size; ++index) {
			int slot = slots[index];
			ItemStack stack = delegate.getStack(slot);

			int amount;
			if (stack.isEmpty())  {
				int amount1 = Math.toIntExact(Math.min(maxAmount, maxCount));
				ItemStack nextStack = ItemContent.asStack(content, amount1);
				if (delegate.canInsert(index, nextStack, direction)) {
					amount = amount1;
					nextStack.setCount(amount);
					context.configure(() -> delegate.setStack(slot, nextStack), () -> delegate.setStack(slot, stack));
				} else
					amount = 0;
			} else if (content.equals(ItemContent.of(stack))) {
				amount = Math.toIntExact(Math.min(maxAmount, maxCount - stack.getCount()));
				incrementalActions.put(stack, amount);
			} else
				amount = 0;

			maxAmount -= amount;
			assert maxAmount >= 0L;
			if (maxAmount == 0L)
				break;
		}

		context.configure(() -> incrementalActions.forEach(ItemStack::increment), () -> incrementalActions.forEach(ItemStack::decrement));
		context.execute(delegate::markDirty);

		return maxAmount;
	}

	@Override
	protected long extract(Context context, Content content, Item type, long maxAmount) {
		long leftoverAmount = maxAmount;

		SidedInventory delegate = getDelegate();
		int[] slots = getSlots();
		int size = slots.length;
		Direction direction = getDirection();

		int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
		Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
				Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
				Util.identityHashStrategy()
		);

		for (int index = 0; index < size; ++index) {
			ItemStack stack = delegate.getStack(slots[index]);
			if (!stack.isEmpty() && content.equals(ItemContent.of(stack)) && delegate.canExtract(index, stack, direction)) {
				// stack is not empty, item matches, can extract
				int amount = Math.toIntExact(Math.min(leftoverAmount, stack.getCount())); // COMMENT should be in int range, negative excluded
				incrementalActions.put(stack, amount);
				leftoverAmount -= amount;
				assert leftoverAmount >= 0L;
				if (leftoverAmount == 0L)
					break;
			}
		}

		context.configure(() -> incrementalActions.forEach(ItemStack::decrement), () -> incrementalActions.forEach(ItemStack::increment));
		context.execute(delegate::markDirty);

		return maxAmount - leftoverAmount;
	}

	@Override
	public Iterator<? extends Atom> getAtomIterator() {
		return getAtomList().iterator();
	}

	@Override
	public long getAtomSize() {
		return getSlots().length;
	}

	@Override
	public long estimateAtomSize() {
		return getAtomSize();
	}

	@Override
	public long getAmount(Content content) {
		return Arrays.stream(getSlots())
				.mapToObj(getDelegate()::getStack)
				.filter(stack -> ItemContent.of(stack).equals(content))
				.mapToLong(ItemStack::getCount)
				.sum();
	}

	@Override
	public Object2LongMap<Content> getAmounts() {
		int[] slots = getSlots();
		return Object2LongMaps.unmodifiable(
				Arrays.stream(slots)
						.mapToObj(getDelegate()::getStack)
						.collect(() -> new Object2LongOpenHashMap<>(slots.length),
								(container, value) -> container.mergeLong(ItemContent.of(value), value.getCount(), Long::sum),
								ViewImplUtilities.getAmountMapsMerger())
		);
	}

	@Override
	public ListModel getDirectModel() {
		return this;
	}

	protected SidedInventory getDelegate() {
		return delegate;
	}

	protected Direction getDirection() {
		return direction;
	}

	protected int[] getSlots() {
		return slots;
	}

	@Override
	public List<? extends Atom> getAtomList() {
		return atomList.get();
	}
}
