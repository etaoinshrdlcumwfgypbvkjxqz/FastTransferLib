package dev.technici4n.fasttransferlib.experimental.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.experimental.impl.base.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.experimental.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.fluid.Fluid;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class LbaFluidToViewParticipant
        extends AbstractMonoCategoryParticipant<Fluid>
        implements View, ListModel {
    private final FixedFluidInv lbaDelegate;
    private final Supplier<? extends List<? extends Atom>> atomList;

    @SuppressWarnings("UnstableApiUsage")
    protected LbaFluidToViewParticipant(FixedFluidInv lbaDelegate) {
        super(Fluid.class);
        this.lbaDelegate = lbaDelegate;
        this.atomList = Suppliers.memoize(() ->
                Streams.stream(getLbaDelegate().tankIterable())
                        .map(SingleFluidTankAtom::of)
                        .collect(ImmutableList.toImmutableList()));
    }

    public static LbaFluidToViewParticipant of(FixedFluidInv lbaDelegate) {
        return new LbaFluidToViewParticipant(lbaDelegate);
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        return SingleFluidTankAtom.insertImpl(getLbaDelegate().getTransferable(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        return SingleFluidTankAtom.extractImpl(getLbaDelegate().getTransferable(), context, content, maxAmount);
    }

    protected FixedFluidInv getLbaDelegate() {
        return lbaDelegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomList().iterator();
    }

    @Override
    public long getAtomSize() {
        return getLbaDelegate().getTankCount();
    }

    @Override
    public long estimateAtomSize() {
        return getAtomSize();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public long getAmount(Content content) {
        return Streams.stream(getLbaDelegate().fluidIterable()).unordered()
                .filter(stack -> content.equals(LbaCompatUtil.asFluidContent(stack)))
                .mapToLong(LbaCompatUtil::asAmount)
                .sum();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Object2LongMap<Content> getAmounts() {
        FixedFluidInv delegate = getLbaDelegate();
        return Streams.stream(delegate.fluidIterable()).unordered()
                .collect(() -> new Object2LongOpenHashMap<>(delegate.getTankCount()),
                        (container, value) -> container.mergeLong(LbaCompatUtil.asFluidContent(value), LbaCompatUtil.asAmount(value), Long::sum),
                        ViewImplUtilities.getAmountMapsMerger());
    }

    @Override
    public ListModel getDirectModel() {
        return this;
    }

    @Override
    public List<? extends Atom> getAtomList() {
        return atomList.get();
    }
}
