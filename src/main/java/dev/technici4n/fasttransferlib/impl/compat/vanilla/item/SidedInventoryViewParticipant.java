package dev.technici4n.fasttransferlib.impl.compat.vanilla.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractComposedViewParticipant;
import dev.technici4n.fasttransferlib.impl.base.transfer.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractMonoCategoryView;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class SidedInventoryViewParticipant
		extends AbstractComposedViewParticipant
		implements ListModel {
	private final SidedInventory delegate;
	private final Direction direction;
	private final int[] slots;
	private final Supplier<? extends List<Atom>> atomList;
	private final View view;
	private final Participant participant;

	@SuppressWarnings("UnstableApiUsage")
	protected SidedInventoryViewParticipant(SidedInventory delegate, Direction direction) {
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

		this.view = new ViewImpl();
		this.participant = new ParticipantImpl();
	}

	private static SidedInventoryViewParticipant of(SidedInventory delegate, Direction direction) {
		return new SidedInventoryViewParticipant(delegate, direction);
	}

	public static AbstractComposedViewParticipant of(Inventory delegate, Direction direction) {
		if (delegate instanceof SidedInventory)
			return of((SidedInventory) delegate, direction);
		return InventoryViewParticipant.of(delegate);
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
	public List<Atom> getAtomList() {
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
		public @NotNull Iterator<Atom> iterator() {
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
		protected long getQuantity(Content content, Item type) {
			return Arrays.stream(getSlots())
					.mapToObj(getDelegate()::getStack)
					.filter(stack -> ItemContent.of(stack).equals(content))
					.mapToLong(ItemStack::getCount)
					.sum();
		}

		@Override
		public Object2LongMap<Content> getQuantitys() {
			int[] slots = getSlots();
			return Object2LongMaps.unmodifiable(
					Arrays.stream(slots)
							.mapToObj(getDelegate()::getStack)
							.collect(() -> new Object2LongOpenHashMap<>(slots.length),
									(container, value) -> container.mergeLong(ItemContent.of(value), value.getCount(), Long::sum),
									ViewImplUtilities.getQuantityMapsMerger())
			);
		}

		@SuppressWarnings("UnstableApiUsage")
		@Override
		public Set<Content> getContents() {
			Inventory delegate = getDelegate();
			int size = delegate.size();
			return IntStream.range(0, size)
					.mapToObj(delegate::getStack)
					.map(ItemContent::of)
					.collect(ImmutableSet.toImmutableSet());
		}

		@Override
		public Model getDirectModel() {
			return SidedInventoryViewParticipant.this;
		}

		@Override
		protected Collection<? extends Class<?>> getSupportedPushEvents() {
			return ImmutableSet.of(); // inventory sucks
		}

		@Override
		protected Collection<? extends Class<?>> getSupportedPullEvents() {
			return ImmutableSet.of(); // inventory sucks
		}

		@Override
		public TriState query(Query query) {
			return TriStateUtilities.orGet(super.query(query), () -> {
				if (query instanceof StoreQuery)
					return TriState.TRUE;
				return TriState.DEFAULT;
			});
		}
	}

	public class ParticipantImpl
			extends AbstractMonoCategoryParticipant<Item> {
		protected ParticipantImpl() {
			super(Item.class);
		}

		@Override
		protected long insertMono(Context context, Content content, Item type, long maxQuantity) {
			SidedInventory delegate = getDelegate();
			int[] slots = getSlots();
			int size = slots.length;
			Direction direction = getDirection();

			int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
			Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
					Math.min(size, Ints.saturatedCast(maxQuantity / maxCount)),
					Util.identityHashStrategy()
			);

			for (int index = 0; index < size; ++index) {
				int slot = slots[index];
				ItemStack stack = delegate.getStack(slot);

				int quantity;
				if (stack.isEmpty())  {
					int quantity1 = Math.toIntExact(Math.min(maxQuantity, maxCount));
					ItemStack nextStack = ItemContent.asStack(content, quantity1);
					if (delegate.canInsert(index, nextStack, direction)) {
						quantity = quantity1;
						nextStack.setCount(quantity);
						context.configure(() -> delegate.setStack(slot, nextStack), () -> delegate.setStack(slot, stack));
					} else
						quantity = 0;
				} else if (content.equals(ItemContent.of(stack))) {
					quantity = Math.toIntExact(Math.min(maxQuantity, maxCount - stack.getCount()));
					incrementalActions.put(stack, quantity);
				} else
					quantity = 0;

				maxQuantity -= quantity;
				assert maxQuantity >= 0L;
				if (maxQuantity == 0L)
					break;
			}

			context.configure(() -> incrementalActions.forEach(ItemStack::increment), () -> incrementalActions.forEach(ItemStack::decrement));
			context.execute(delegate::markDirty);

			return maxQuantity;
		}

		@Override
		protected long extractMono(Context context, Content content, Item type, long maxQuantity) {
			long leftoverQuantity = maxQuantity;

			SidedInventory delegate = getDelegate();
			int[] slots = getSlots();
			int size = slots.length;
			Direction direction = getDirection();

			int maxCount = Math.min(delegate.getMaxCountPerStack(), type.getMaxCount());
			Object2IntMap<ItemStack> incrementalActions = new Object2IntOpenCustomHashMap<>(
					Math.min(size, Ints.saturatedCast(maxQuantity / maxCount)),
					Util.identityHashStrategy()
			);

			for (int index = 0; index < size; ++index) {
				ItemStack stack = delegate.getStack(slots[index]);
				if (!stack.isEmpty() && content.equals(ItemContent.of(stack)) && delegate.canExtract(index, stack, direction)) {
					// stack is not empty, item matches, can extract
					int quantity = Math.toIntExact(Math.min(leftoverQuantity, stack.getCount())); // COMMENT should be in int range, negative excluded
					incrementalActions.put(stack, quantity);
					leftoverQuantity -= quantity;
					assert leftoverQuantity >= 0L;
					if (leftoverQuantity == 0L)
						break;
				}
			}

			context.configure(() -> incrementalActions.forEach(ItemStack::decrement), () -> incrementalActions.forEach(ItemStack::increment));
			context.execute(delegate::markDirty);

			return maxQuantity - leftoverQuantity;
		}

		@Override
		public TriState query(Query query) {
			return TriStateUtilities.orGet(super.query(query), () -> {
				if (query instanceof TransferQuery)
					return TriState.TRUE;
				return TriState.DEFAULT;
			});
		}
	}
}
