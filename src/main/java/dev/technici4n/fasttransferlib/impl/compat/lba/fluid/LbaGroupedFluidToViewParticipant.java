package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.model.MapModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatImplUtil;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.fluid.Fluid;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LbaGroupedFluidToViewParticipant
        extends AbstractMonoCategoryViewParticipant<Fluid>
        implements View, MapModel {
    private final GroupedFluidInvView delegate;

    protected LbaGroupedFluidToViewParticipant(GroupedFluidInvView delegate) {
        super(Fluid.class);
        this.delegate = delegate;
    }

    public static LbaGroupedFluidToViewParticipant of(GroupedFluidInvView delegate) {
        return new LbaGroupedFluidToViewParticipant(delegate);
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        return LbaCompatImplUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        return LbaCompatImplUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
    }

    protected GroupedFluidInvView getDelegate() {
        return delegate;
    }

    @Override
    public Iterator<? extends Atom> getAtomIterator() {
        return getAtomMap().values().iterator();
    }

    @Override
    public long getAtomSize() {
        return getDelegate().getStoredFluids().size();
    }

    @Override
    public long estimateAtomSize() {
        return getAtomSize();
    }

    @Override
    protected long getAmount(Content content, Fluid type) {
        // must be fluid key
        return LbaCompatUtil.asAmount(getDelegate().getAmount_F(LbaCompatUtil.asFluidKey(content)));
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
        return getDelegate().getStoredFluids().stream()
                .map(LbaCompatUtil::asFluidContent)
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Model getDirectModel() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Content, ? extends Atom> getAtomMap() {
        GroupedFluidInvView delegate = getDelegate();
        return Maps.toMap((Iterable<Content>) getContents(), content -> {
            assert content != null;
            return MonoGroupedFluidInvAtom.of(delegate, content);
        });
    }
}
