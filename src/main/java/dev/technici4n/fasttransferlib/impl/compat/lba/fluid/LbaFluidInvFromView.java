package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.context.TransactionContext;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import dev.technici4n.fasttransferlib.impl.util.ViewUtilities;
import net.minecraft.fluid.Fluid;

import java.util.*;

public class LbaFluidInvFromView
        implements GroupedFluidInv, FixedFluidInv {
    private final View delegate;

    protected LbaFluidInvFromView(View delegate) {
        // the use of 'FluidFilter' means a view is required
        this.delegate = delegate;
    }

    public static LbaFluidInvFromView of(View delegate) {
        return new LbaFluidInvFromView(delegate);
    }

    protected View getDelegate() {
        return delegate;
    }

    @Override
    public FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) {
        Context context = LbaCompatUtil.asStatelessContext(simulation);

        long insert = LbaCompatUtil.asAmount(fluid);
        long leftover = ViewUtilities.insert(getDelegate(),
                context,
                LbaCompatUtil.asFluidContent(fluid),
                insert);
        long inserted = insert - leftover;

        return fluid.withAmount(fluid.getAmount_F().sub(LbaCompatUtil.asFluidAmount(inserted)));
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        Context context = LbaCompatUtil.asStatelessContext(simulation);

        Content[] contentLock = {null};
        long extracted = ViewUtilities.extract(getDelegate(),
                context,
                LbaCompatUtil.asAmount(maxAmount),
                atom -> {
                    Content content = atom.getContent();
                    if (content.isEmpty() || content.getCategory() != Fluid.class)
                        return null;
                    Content contentLock1 = contentLock[0];
                    if (contentLock1 == null && filter.matches(LbaCompatUtil.asFluidKey(content))) {
                        return contentLock[0] = content;
                    }
                    return contentLock1;
                });

        Content contentLock1 = contentLock[0];
        if (contentLock1 == null)
            return FluidKeys.EMPTY.withAmount(LbaCompatUtil.asFluidAmount(extracted));
        assert contentLock1.getCategory() == Fluid.class;
        return LbaCompatUtil.asFluidVolume(contentLock1, extracted);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Set<FluidKey> getStoredFluids() {
        return getDelegate().getContents().stream()
                .map(LbaCompatUtil::asFluidKey)
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public FluidInvStatistic getStatistics(FluidFilter filter) {
        @SuppressWarnings("UnstableApiUsage")
        List<? extends Atom> filtered = Streams.stream(getDelegate().getAtomIterator())
                .filter(atom -> filter.matches(LbaCompatUtil.asFluidKey(atom.getContent())))
                .collect(ImmutableList.toImmutableList());
        long amount = filtered.stream()
                .mapToLong(Atom::getAmount)
                .sum();
        OptionalLong spaceTotal = filtered.stream()
                .map(Atom::getCapacity)
                .filter(OptionalLong::isPresent)
                .mapToLong(OptionalLong::getAsLong)
                .reduce(Long::sum);
        return new FluidInvStatistic(filter,
                LbaCompatUtil.asFluidAmount(amount),
                LbaCompatUtil.asFluidAmount(spaceTotal.orElse(amount) - amount),
                spaceTotal.isPresent() ? LbaCompatUtil.asFluidAmount(spaceTotal.getAsLong()) : FluidAmount.NEGATIVE_ONE);
    }

    @Override
    public int getTankCount() {
        return getDelegateListModel(this)
                .<Collection<? extends Atom>>map(ListModel::getAtomList)
                .orElseGet(ImmutableSet::of)
                .size();
    }

    @Override
    public FluidVolume getInvFluid(int tank) {
        ensureIndexInBounds(this, tank);
        Atom atom = getDelegateListModel(this)
                .orElseThrow(AssertionError::new)
                .getAtomList()
                .get(tank);
        return LbaCompatUtil.asFluidVolume(
                atom.getContent(),
                atom.getAmount()
        );
    }

    @Override
    public boolean isFluidValidForTank(int tank, FluidKey fluid) {
        return true; // just return true, permitted by the contract
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean setInvFluid(int tank, FluidVolume to, Simulation simulation) {
        ensureIndexInBounds(this, tank);

        Atom atom = getDelegateListModel(this)
                .orElseThrow(AssertionError::new)
                .getAtomList()
                .get(tank);

        if (to.isEmpty()) {
            // extract only
            try (TransactionContext transaction = new TransactionContext(2L)) {
                TransferUtilities.extractAll(transaction, atom);
                if (atom.getAmount() == 0L) {
                    transaction.commitWith(LbaCompatUtil.asStatelessContext(simulation));
                    return true;
                }
                return false;
            }
        } else {
            Content atomContent = atom.getContent();
            Content toContent = LbaCompatUtil.asFluidContent(to);
            if (atomContent.isEmpty()) {
                // insert only
                try (TransactionContext transaction = new TransactionContext(1L)) {
                    if (Streams.stream(LbaCompatUtil.FluidAmountAsAmountsIterator.of(to))
                            .mapToLong(insert -> atom.insert(transaction, toContent, insert))
                            .allMatch(leftover -> leftover == 0L)) {
                        transaction.commitWith(LbaCompatUtil.asStatelessContext(simulation));
                        return true;
                    }
                    return false;
                }
            } else if (atomContent.equals(toContent)) {
                    // extract or insert
                    FluidAmount atomAmount = LbaCompatUtil.asFluidAmount(atom.getAmount());
                    FluidAmount toAmount = to.getAmount_F();

                    boolean extract;
                    if (atomAmount.isGreaterThan(toAmount)) extract = true;
                    else if (atomAmount.isLessThan(toAmount)) extract = false;
                    else return true;

                    FluidAmount diff = LbaCompatUtil.abs(atomAmount.sub(toAmount));
                    assert !diff.isZero();

                    try (TransactionContext transaction = new TransactionContext(1L)) {
                        boolean result;
                        if (extract) {
                            result = Streams.stream(LbaCompatUtil.FluidAmountAsAmountsIterator.of(diff))
                                    .allMatch(diff1 -> atom.extract(transaction, atomContent, diff1) == diff1);
                        } else {
                            result = Streams.stream(LbaCompatUtil.FluidAmountAsAmountsIterator.of(diff))
                                    .mapToLong(diff1 -> atom.insert(transaction, atomContent, diff1))
                                    .allMatch(leftover -> leftover == 0L);
                        }
                        if (result)
                            transaction.commitWith(LbaCompatUtil.asStatelessContext(simulation));
                        return result;
                    }
            } else {
                // extract and then insert
                try (TransactionContext transaction = new TransactionContext(2L)) {
                    TransferUtilities.extractAll(transaction, atom);
                    if (atom.getAmount() == 0L
                            && Streams.stream(LbaCompatUtil.FluidAmountAsAmountsIterator.of(to))
                            .mapToLong(insert -> atom.insert(transaction, toContent, insert))
                            .allMatch(leftover -> leftover == 0L)) {
                        transaction.commitWith(LbaCompatUtil.asStatelessContext(simulation));
                        return true;
                    }
                    return false;
                }
            }
        }
    }

    protected static Optional<? extends ListModel> getDelegateListModel(LbaFluidInvFromView instance) {
        Model model = instance.getDelegate().getDirectModel();
        if (model instanceof ListModel)
            return Optional.of((ListModel) model);
        return Optional.empty();
    }

    protected static void ensureIndexInBounds(LbaFluidInvFromView instance, int index) {
        if (index >= instance.getTankCount() || index < 0)
            throw new IndexOutOfBoundsException(String.valueOf(index));
    }
}
