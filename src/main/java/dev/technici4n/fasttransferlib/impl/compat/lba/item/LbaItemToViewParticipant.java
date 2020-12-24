package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.ViewImplUtilities;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class LbaItemToViewParticipant
        extends AbstractMonoCategoryParticipant<Item>
        implements View, ListModel {
    private final FixedItemInv delegate;
    private final Supplier<? extends List<? extends Atom>> atomList;

    @SuppressWarnings("UnstableApiUsage")
    protected LbaItemToViewParticipant(FixedItemInv delegate) {
        super(Item.class);
        this.delegate = delegate;
        this.atomList = Suppliers.memoize(() ->
                Streams.stream(getDelegate().slotIterable())
                        .map(SingleItemSlotAtom::of)
                        .collect(ImmutableList.toImmutableList()));
    }

    public static LbaItemToViewParticipant of(FixedItemInv delegate) {
        return new LbaItemToViewParticipant(delegate);
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        return SingleItemSlotAtom.insertImpl(getDelegate().getTransferable(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        return SingleItemSlotAtom.extractImpl(getDelegate().getTransferable(), context, content, maxAmount);
    }

    protected FixedItemInv getDelegate() {
        return delegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomList().iterator();
    }

    @Override
    public long getAtomSize() {
        return getDelegate().getSlotCount();
    }

    @Override
    public long estimateAtomSize() {
        return getAtomSize();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public long getAmount(Content content) {
        return Streams.stream(getDelegate().stackIterable()).unordered()
                .filter(stack -> content.equals(ItemContent.of(stack)))
                .mapToLong(ItemStack::getCount)
                .sum();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Object2LongMap<Content> getAmounts() {
        FixedItemInv delegate = getDelegate();
        return Streams.stream(delegate.stackIterable()).unordered()
                .collect(() -> new Object2LongOpenHashMap<>(delegate.getSlotCount()),
                        (container, value) -> container.mergeLong(ItemContent.of(value), value.getCount(), Long::sum),
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
