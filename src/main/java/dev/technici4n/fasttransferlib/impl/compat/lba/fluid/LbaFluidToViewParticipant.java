package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.fluid.Fluid;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class LbaFluidToViewParticipant
        extends AbstractMonoCategoryParticipant<Fluid>
        implements View, ListModel {
    private final FixedFluidInv delegate;
    private final Supplier<? extends List<? extends Atom>> atomList;

    @SuppressWarnings("UnstableApiUsage")
    protected LbaFluidToViewParticipant(FixedFluidInv delegate) {
        super(Fluid.class);
        this.delegate = delegate;
        this.atomList = Suppliers.memoize(() ->
                Streams.stream(getDelegate().tankIterable())
                        .map(SingleFluidTankAtom::of)
                        .collect(ImmutableList.toImmutableList()));
    }

    public static LbaFluidToViewParticipant of(FixedFluidInv delegate) {
        return new LbaFluidToViewParticipant(delegate);
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        return SingleFluidTankAtom.insertImpl(getDelegate().getTransferable(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        return SingleFluidTankAtom.extractImpl(getDelegate().getTransferable(), context, content, maxAmount);
    }

    protected FixedFluidInv getDelegate() {
        return delegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomList().iterator();
    }

    @Override
    public long getAtomSize() {
        return getDelegate().getTankCount();
    }

    @Override
    public long estimateAtomSize() {
        return getAtomSize();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public long getAmount(Content content) {
        return Streams.stream(getDelegate().fluidIterable()).unordered()
                .filter(stack -> content.equals(LbaCompatUtil.asFluidContent(stack)))
                .mapToLong(LbaCompatUtil::asAmount)
                .sum();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Object2LongMap<Content> getAmounts() {
        FixedFluidInv delegate = getDelegate();
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
