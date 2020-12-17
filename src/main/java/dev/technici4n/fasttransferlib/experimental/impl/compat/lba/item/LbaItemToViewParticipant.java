package dev.technici4n.fasttransferlib.experimental.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.model.ListModel;
import dev.technici4n.fasttransferlib.experimental.impl.base.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.experimental.impl.util.ViewImplUtilities;
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
    private final FixedItemInv lbaDelegate;
    private final Supplier<? extends List<? extends Atom>> atomList;

    @SuppressWarnings("UnstableApiUsage")
    protected LbaItemToViewParticipant(FixedItemInv lbaDelegate) {
        super(Item.class);
        this.lbaDelegate = lbaDelegate;
        this.atomList = Suppliers.memoize(() ->
                Streams.stream(getLbaDelegate().slotIterable())
                        .map(SingleItemSlotAtom::of)
                        .collect(ImmutableList.toImmutableList()));
    }

    public static LbaItemToViewParticipant of(FixedItemInv lbaDelegate) {
        return new LbaItemToViewParticipant(lbaDelegate);
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        return SingleItemSlotAtom.insertImpl(getLbaDelegate().getTransferable(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        return SingleItemSlotAtom.extractImpl(getLbaDelegate().getTransferable(), context, content, maxAmount);
    }

    protected FixedItemInv getLbaDelegate() {
        return lbaDelegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomList().iterator();
    }

    @Override
    public long getAtomSize() {
        return getLbaDelegate().getSlotCount();
    }

    @Override
    public long estimateAtomSize() {
        return getAtomSize();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public long getAmount(Content content) {
        return Streams.stream(getLbaDelegate().stackIterable()).unordered()
                .filter(stack -> content.equals(ItemContent.of(stack)))
                .mapToLong(ItemStack::getCount)
                .sum();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Object2LongMap<Content> getAmounts() {
        FixedItemInv delegate = getLbaDelegate();
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
