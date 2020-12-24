package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.GroupedItemInvView;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.model.MapModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatImplUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.item.Item;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LbaGroupedItemToViewParticipant
        extends AbstractMonoCategoryViewParticipant<Item>
        implements MapModel {
    private final GroupedItemInvView delegate;

    protected LbaGroupedItemToViewParticipant(GroupedItemInvView delegate) {
        super(Item.class);
        this.delegate = delegate;
    }

    public static LbaGroupedItemToViewParticipant of(GroupedItemInvView delegate) {
        return new LbaGroupedItemToViewParticipant(delegate);
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        return LbaCompatImplUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        return LbaCompatImplUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
    }

    protected GroupedItemInvView getDelegate() {
        return delegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomMap().values().iterator();
    }

    @Override
    public long getAtomSize() {
        return getDelegate().getStoredStacks().size();
    }

    @Override
    public long estimateAtomSize() {
        return getAtomSize();
    }

    @Override
    protected long getAmount(Content content, Item type) {
        return getDelegate().getAmount(ItemContent.asStack(content, 1));
    }

    @Override
    public Object2LongMap<Content> getAmounts() {
        return Object2LongMaps.unmodifiable(
                new Object2LongOpenHashMap<>(Maps.toMap(getContents(), this::getAmount))
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Set<? extends Content> getContents() {
        return getDelegate().getStoredStacks().stream()
                .map(ItemContent::of)
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Model getDirectModel() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Content, ? extends Atom> getAtomMap() {
        GroupedItemInvView delegate = getDelegate();
        return Maps.toMap((Iterable<Content>) getContents(), content -> {
            assert content != null;
            return MonoGroupedItemInvAtom.of(delegate, content);
        });
    }
}
