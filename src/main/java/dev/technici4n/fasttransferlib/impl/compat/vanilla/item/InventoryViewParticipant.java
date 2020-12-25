package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractComposedViewParticipant;
import dev.technici4n.fasttransferlib.impl.base.transfer.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractMonoCategoryView;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class InventoryViewParticipant
		extends AbstractComposedViewParticipant
		implements ListModel {
	private final Inventory delegate;
	private final Supplier<? extends List<? extends Atom>> atomList;
	private final View view;
	private final Participant participant;

	@SuppressWarnings("UnstableApiUsage")
	protected InventoryViewParticipant(Inventory delegate) {
		this.delegate = delegate;
		this.atomList = Suppliers.memoize(() -> {
			Inventory delegate1 = getDelegate();
			return IntStream.range(0, delegate1.size())
					.mapToObj(slot -> new InventorySlotAtom(delegate1, slot))
					.collect(ImmutableList.toImmutableList());
		});

		this.view = new ViewImpl();
		this.participant = new ParticipantImpl();
	}

	static InventoryViewParticipant of(Inventory delegate) {
		return new InventoryViewParticipant(delegate);
	}

	protected Inventory getDelegate() {
		return delegate;
	}

	@Override
	public List<? extends Atom> getAtomList() {
		return atomList.get();
	}

	@Override
	protected View getView() {
		return view;
	}

	@Override
	protected Participant getParticipant() {
		return participant;
	}

	public class ViewImpl
			extends AbstractMonoCategoryView<Item> {
		protected ViewImpl() {
			super(Item.class);
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
		protected long getAmount(Content content, Item type) {
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
							.collect(() -> new Object2LongOpenHashMap<>(size),
									(container, value) -> container.mergeLong(ItemContent.of(value), value.getCount(), Long::sum),
									ViewImplUtilities.getAmountMapsMerger())
			);
		}

		@SuppressWarnings("UnstableApiUsage")
		@Override
		public Set<? extends Content> getContents() {
			Inventory delegate = getDelegate();
			int size = delegate.size();
			return IntStream.range(0, size)
					.mapToObj(delegate::getStack)
					.map(ItemContent::of)
					.collect(ImmutableSet.toImmutableSet());
		}

		@Override
		public Model getDirectModel() {
			return InventoryViewParticipant.this;
		}

		@Override
		protected Collection<? extends Class<?>> getSupportedPushNotifications() {
			return ImmutableSet.of(); // inventory sucks
		}

		@Override
		protected boolean supportsPullNotification() {
			return false; // inventory sucks
		}
	}

	public class ParticipantImpl
			extends AbstractMonoCategoryParticipant<Item> {
		protected ParticipantImpl() {
			super(Item.class);
		}

		@Override
		protected long insertMono(Context context, Content content, Item type, long maxAmount) {
			Inventory delegate = getDelegate();
			int size = delegate.size();

			int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
			Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
					Math.min(size, Ints.saturatedCast(maxAmount / maxCount)),
					Util.identityHashStrategy()
			);

			// transaction not needed - each slot only contributes to one operation and slots are independent of each other
			for (int index = 0; index < size; ++index) {
				int slot = index;
				ItemStack stack = delegate.getStack(slot);

				int amount;
				if (stack.isEmpty())  {
					amount = Math.toIntExact(Math.min(maxAmount, maxCount));
					context.configure(
							() -> delegate.setStack(slot, ItemContent.asStack(content, amount)),
							() -> delegate.setStack(slot, stack)
					);
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
		protected long extractMono(Context context, Content content, Item type, long maxAmount) {
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
				if (!stack.isEmpty() && content.equals(ItemContent.of(stack))) {
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
	}
}
