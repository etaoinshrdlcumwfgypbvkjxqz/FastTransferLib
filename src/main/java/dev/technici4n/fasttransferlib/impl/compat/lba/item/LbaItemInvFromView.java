package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.GroupedItemInv;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import dev.technici4n.fasttransferlib.impl.util.ViewUtilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.math.BigInteger;
import java.util.*;

public class LbaItemInvFromView
        implements GroupedItemInv, FixedItemInv {
    private final View delegate;

    protected LbaItemInvFromView(View delegate) {
        // the use of 'FluidFilter' means a view is required
        this.delegate = delegate;
    }

    public static LbaItemInvFromView of(View delegate) {
        return new LbaItemInvFromView(delegate);
    }

    protected View getDelegate() {
        return delegate;
    }

    @Override
    public ItemStack attemptExtraction(ItemFilter itemFilter, int maxAmount, Simulation simulation) {
        Context context = LbaCompatUtil.asStatelessContext(simulation);

        Content[] contentLock = {null};
        long extracted = ViewUtilities.extract(getDelegate(),
                context,
                maxAmount,
                atom -> {
                    Content content = atom.getContent();
                    if (content.isEmpty() || content.getCategory() != Item.class)
                        return null;
                    Content contentLock1 = contentLock[0];
                    if (contentLock1 == null && itemFilter.matches(ItemContent.asStack(content, 1))) {
                        return contentLock[0] = content;
                    }
                    return contentLock1;
                });
        int extracted1 = Math.toIntExact(extracted); // within int range

        Content contentLock1 = contentLock[0];
        if (contentLock1 == null)
            return ItemStack.EMPTY;
        assert contentLock1.getCategory() == Item.class;
        return ItemContent.asStack(contentLock1, extracted1);
    }

    @Override
    public ItemStack attemptInsertion(ItemStack itemStack, Simulation simulation) {
        Context context = LbaCompatUtil.asStatelessContext(simulation);

        Content content = ItemContent.of(itemStack);
        long leftover = ViewUtilities.insert(getDelegate(),
                context,
                content,
                itemStack.getCount());
        int leftover1 = Math.toIntExact(leftover); // within int range

        return ItemContent.asStack(content, leftover1);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Set<ItemStack> getStoredStacks() {
        return getDelegate().getContents().stream()
                .map(content -> ItemContent.asStack(content, 1))
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public int getTotalCapacity() {
        return Ints.saturatedCast(
                Streams.stream(getDelegate().getAtomIterator())
                        .map(Atom::getCapacity)
                        .filter(OptionalLong::isPresent)
                        .mapToLong(OptionalLong::getAsLong)
                        .sum()
        );
    }

    @Override
    public ItemInvStatistic getStatistics(ItemFilter filter) {
        @SuppressWarnings("UnstableApiUsage")
        List<? extends Atom> filtered = Streams.stream(getDelegate().getAtomIterator())
                .filter(atom -> filter.matches(ItemContent.asStack(atom.getContent(), 1)))
                .collect(ImmutableList.toImmutableList());
        long amount = filtered.stream()
                .mapToLong(Atom::getAmount)
                .sum();
        OptionalLong spaceTotal = filtered.stream()
                .map(Atom::getCapacity)
                .filter(OptionalLong::isPresent)
                .mapToLong(OptionalLong::getAsLong)
                .reduce(Long::sum);
        return new ItemInvStatistic(filter,
                Ints.saturatedCast(amount),
                Ints.saturatedCast(spaceTotal.orElse(amount) - amount),
                spaceTotal.isPresent() ? Ints.saturatedCast(spaceTotal.getAsLong()) : -1);
    }

    @Override
    public int getSlotCount() {
        return getDelegateListModel(this)
                .<Collection<? extends Atom>>map(ListModel::getAtomList)
                .orElseGet(ImmutableSet::of)
                .size();
    }

    @Override
    public ItemStack getInvStack(int slot) {
        ensureIndexInBounds(this, slot);
        Atom atom = getDelegateListModel(this)
                .orElseThrow(AssertionError::new)
                .getAtomList()
                .get(slot);
        return ItemContent.asStack(atom.getContent(), Ints.saturatedCast(atom.getAmount()));
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        ensureIndexInBounds(this, slot);
        return true; // just return true, permitted by the contract
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        ensureIndexInBounds(this, slot);
        return TransferUtilities.setAtomContent(LbaCompatUtil.asStatelessContext(simulation),
                getDelegateListModel(this)
                        .orElseThrow(AssertionError::new)
                        .getAtomList()
                        .get(slot),
                ItemContent.of(to),
                BigInteger.valueOf(to.getCount()));
    }

    protected static Optional<? extends ListModel> getDelegateListModel(LbaItemInvFromView instance) {
        Model model = instance.getDelegate().getDirectModel();
        if (model instanceof ListModel)
            return Optional.of((ListModel) model);
        return Optional.empty();
    }

    protected static void ensureIndexInBounds(LbaItemInvFromView instance, int index) {
        if (index >= instance.getSlotCount() || index < 0)
            throw new IndexOutOfBoundsException(String.valueOf(index));
    }
}
