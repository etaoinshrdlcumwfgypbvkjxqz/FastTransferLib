package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.FixedFluidInvView;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class LbaFixedFluidToViewParticipant
        extends LbaGroupedFluidToViewParticipant
        implements ListModel {
    private final FixedFluidInvView fixedDelegate;
    private final Supplier<? extends List<? extends Atom>> atomList;

    @SuppressWarnings("UnstableApiUsage")
    protected LbaFixedFluidToViewParticipant(FixedFluidInvView delegate) {
        super(delegate.getGroupedInv());
        this.fixedDelegate = delegate;
        this.atomList = Suppliers.memoize(() ->
                Streams.stream(getFixedDelegate().tankIterable())
                        .map(SingleFluidTankAtom::of)
                        .collect(ImmutableList.toImmutableList()));
    }

    public static LbaFixedFluidToViewParticipant of(FixedFluidInvView delegate) {
        return new LbaFixedFluidToViewParticipant(delegate);
    }

    protected FixedFluidInvView getFixedDelegate() {
        return fixedDelegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomList().iterator();
    }

    @Override
    public long getAtomSize() {
        return getFixedDelegate().getTankCount();
    }

    @Override
    public List<? extends Atom> getAtomList() {
        return atomList.get();
    }
}
