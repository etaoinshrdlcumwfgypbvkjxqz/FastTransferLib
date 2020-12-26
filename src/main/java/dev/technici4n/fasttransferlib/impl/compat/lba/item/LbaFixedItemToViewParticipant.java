package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.FixedItemInvView;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class LbaFixedItemToViewParticipant
        extends LbaGroupedItemToViewParticipant
        implements ListModel {
    private final FixedItemInvView fixedDelegate;
    private final Supplier<? extends List<Atom>> atomList;

    @SuppressWarnings("UnstableApiUsage")
    protected LbaFixedItemToViewParticipant(FixedItemInvView delegate) {
        super(delegate.getGroupedInv());
        this.fixedDelegate = delegate;
        this.atomList = Suppliers.memoize(() ->
                Streams.stream(getFixedDelegate().slotIterable())
                        .map(SingleItemSlotAtom::of)
                        .collect(ImmutableList.toImmutableList()));
    }

    public static LbaFixedItemToViewParticipant of(FixedItemInvView delegate) {
        return new LbaFixedItemToViewParticipant(delegate);
    }

    protected FixedItemInvView getFixedDelegate() {
        return fixedDelegate;
    }

    @Override
    public @NotNull Iterator<Atom> iterator() {
        return getAtomList().iterator();
    }

    @Override
    public long getAtomSize() {
            return getFixedDelegate().getSlotCount();
    }

    @Override
    public List<Atom> getAtomList() {
        return atomList.get();
    }
}
