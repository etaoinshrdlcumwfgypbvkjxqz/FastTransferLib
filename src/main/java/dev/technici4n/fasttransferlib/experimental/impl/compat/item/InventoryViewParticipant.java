package dev.technici4n.fasttransferlib.experimental.impl.compat.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.participant.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class InventoryViewParticipant
		extends AbstractMonoCategoryParticipant<Item>
		implements View, ListModel {
	private final Inventory delegate;
	private final Supplier<? extends List<? extends Atom>> atomList;

	@SuppressWarnings("UnstableApiUsage")
	protected InventoryViewParticipant(Inventory delegate) {
		super(Item.class);
		this.delegate = delegate;
		this.atomList = Suppliers.memoize(() -> {
			Inventory delegate1 = getDelegate();
			return IntStream.range(0, delegate1.size())
					.mapToObj(slot -> new InventorySlotAtom(delegate1, slot))
					.collect(ImmutableList.toImmutableList());
		});
	}

	static InventoryViewParticipant of(Inventory delegate) {
		return new InventoryViewParticipant(delegate);
	}

	@Override
	protected long insert(Context context, Content content, Item type, long maxAmount) {
		Inventory delegate = getDelegate();
		int size = delegate.size();

		int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
		Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
				Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
				Util.identityHashStrategy()
		);

		for (int index = 0; index < size; ++index) {
			int slot = index;
			ItemStack stack = delegate.getStack(slot);

			int amount;
			if (stack.isEmpty())  {
				amount = Math.toIntExact(Math.min(maxAmount, maxCount));
				context.execute(() -> {
					delegate.setStack(slot, new ItemStack(type, amount));
					delegate.markDirty();
				}, () -> {
					delegate.setStack(slot, stack);
					delegate.markDirty();
				});
			} else if (stack.getItem() == type) {
				amount = Math.toIntExact(Math.min(maxAmount, maxCount - stack.getCount()));
				incrementalActions.put(stack, amount);
			} else
				amount = 0;

			maxAmount -= amount;
			assert maxAmount >= 0L;
			if (maxAmount == 0L)
				break;
		}

		context.execute(() -> {
			incrementalActions.forEach(ItemStack::increment);
			if (!incrementalActions.isEmpty())
				delegate.markDirty();
		}, () -> {
			incrementalActions.forEach(ItemStack::decrement);
			if (!incrementalActions.isEmpty())
				delegate.markDirty();
		});

		return maxAmount;
	}

	@Override
	protected long extract(Context context, Content content, Item type, long maxAmount) {
		long leftoverAmount = maxAmount;

		Inventory delegate = getDelegate();
		int size = delegate.size();

		int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
		Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
				Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
				Util.identityHashStrategy()
		);

		for (int index = 0; index < size; ++index) {
			ItemStack stack = delegate.getStack(index);
			if (!stack.isEmpty() && stack.getItem() == type) {
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
				delegate.markDirty();
		}, () -> {
			incrementalActions.forEach(ItemStack::increment);
			if (!incrementalActions.isEmpty())
				delegate.markDirty();
		});

		return maxAmount - leftoverAmount;
	}

	@Override
	public Iterator<? extends Atom> getAtomIterator() {
		return getAtomList().iterator();
	}

	@Override
	public long getAtomSize() {
		return getDelegate().size();
	}

	@Override
	public long estimateAtomSize() {
		return getAtomSize();
	}

	@Override
	public long getAmount(Content content) {
		Inventory delegate = getDelegate();
		return IntStream.range(0, delegate.size())
				.mapToObj(delegate::getStack)
				.filter(stack -> ItemContent.of(stack).equals(content))
				.mapToLong(ItemStack::getCount)
				.sum();
	}

	@Override
	public Object2LongMap<Content> getAmounts() {
		Inventory delegate = getDelegate();
		int size = delegate.size();
		return Object2LongMaps.unmodifiable(
				IntStream.range(0, size)
						.mapToObj(delegate::getStack)
						.collect(() -> new Object2LongOpenHashMap<Content>(size),
								(container, value) -> container.mergeLong(ItemContent.of(value), value.getCount(), Long::sum),
								ViewImplUtilities.getAmountMapsMerger())
		);
	}

	@Override
	public ListModel getDirectModel() {
		return this;
	}

	protected Inventory getDelegate() {
		return delegate;
	}

	@Override
	public List<? extends Atom> getAtomList() {
		return atomList.get();
	}
}
