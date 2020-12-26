package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.event.NetTransferEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;
import dev.technici4n.fasttransferlib.api.view.model.MapModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractComposedViewParticipant;
import dev.technici4n.fasttransferlib.impl.base.transfer.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractMonoCategoryView;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.view.event.TransferEventImpl;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.NotNull;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LbaGroupedFluidToViewParticipant
        extends AbstractComposedViewParticipant
        implements MapModel {
    private final GroupedFluidInvView delegate;
    private final View view;
    private final Participant participant;
    private static final Set<Class<?>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(TransferEvent.class);
    private static final Set<Class<?>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, NetTransferEvent.class);

    protected LbaGroupedFluidToViewParticipant(GroupedFluidInvView delegate) {
        this.delegate = delegate;
        this.view = new ViewImpl(this.delegate);
        this.participant = new ParticipantImpl();
    }

    public static LbaGroupedFluidToViewParticipant of(GroupedFluidInvView delegate) {
        return new LbaGroupedFluidToViewParticipant(delegate);
    }

    protected GroupedFluidInvView getDelegate() {
        return delegate;
    }

    @Override
    protected View getView() {
        return view;
    }

    @Override
    protected Participant getParticipant() {
        return participant;
    }

    @Override
    public Map<Content, Atom> getAtomMap() {
        GroupedFluidInvView delegate = getDelegate();
        return Maps.toMap(getContents(), content -> {
            assert content != null;
            return MonoGroupedFluidInvAtom.of(delegate, content);
        });
    }

    public class ViewImpl
            extends AbstractMonoCategoryView<Fluid> {
        private boolean hasTransferListener;

        protected ViewImpl(GroupedFluidInvView delegate) {
            super(Fluid.class);

            OptionalWeakReference<ViewImpl> weakThis = OptionalWeakReference.of(this);
            ListenerToken listenerToken = delegate.addListener_F((inv, fluid, previous, current) -> weakThis.getOptional()
                            .ifPresent(this1 -> {
                                Content content1 = LbaCompatUtil.asFluidContent(fluid);

                                FluidAmount diff = current.sub(previous);
                                if (diff.isZero())
                                    return;

                                TransferAction action = TransferAction.fromDifference(diff.isPositive());
                                TransferUtilities.BigIntegerAsLongIterator.ofStream(LbaCompatUtil.asBigAmount(LbaCompatUtil.abs(diff)))
                                        .mapToObj(diff1 -> TransferEventImpl.of(action, content1, diff1))
                                        .forEach(data -> {
                                            this1.revise(NetTransferEvent.class);
                                            this1.reviseAndNotify(TransferEvent.class, data);
                                        });
                            }),
                    () -> weakThis.getOptional().ifPresent(ViewImpl::onTransferListenerRemoved));

            if (listenerToken == null) {
                this.hasTransferListener = false;
            } else {
                this.hasTransferListener = true;
                Cleaner.create(this, listenerToken::removeListener);
            }
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
        public Set<Content> getContents() {
            return getDelegate().getStoredFluids().stream()
                    .map(LbaCompatUtil::asFluidContent)
                    .collect(ImmutableSet.toImmutableSet());
        }

        @Override
        public Model getDirectModel() {
            return LbaGroupedFluidToViewParticipant.this;
        }

        @Override
        public @NotNull Iterator<Atom> iterator() {
            return getAtomMap().values().iterator();
        }

        @Override
        protected Collection<? extends Class<?>> getSupportedPushEvents() {
            return isHasTransferListener() ? SUPPORTED_PUSH_EVENTS : ImmutableSet.of();
        }

        @Override
        protected Collection<? extends Class<?>> getSupportedPullEvents() {
            return isHasTransferListener() ? SUPPORTED_PULL_EVENTS : ImmutableSet.of();
        }

        protected boolean isHasTransferListener() {
            return hasTransferListener;
        }

        protected void setHasTransferListener(@SuppressWarnings("SameParameterValue") boolean hasTransferListener) {
            this.hasTransferListener = hasTransferListener;
        }

        protected void onTransferListenerRemoved() {
            setHasTransferListener(false);
            getPublisherIfPresent(TransferEvent.class).ifPresent(EmittingPublisher::clearSubscribers);
        }

        @Override
        public TriState query(Query query) {
            return TriStateUtilities.orGet(super.query(query), () -> {
                if (query instanceof StoreQuery)
                    return TriState.TRUE;
                return TriState.DEFAULT;
            });
        }
    }

    public class ParticipantImpl
            extends AbstractMonoCategoryParticipant<Fluid> {
        protected ParticipantImpl() {
            super(Fluid.class);
        }

        @Override
        protected long insertMono(Context context, Content content, Fluid type, long maxAmount) {
            return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
        }

        @Override
        protected long extractMono(Context context, Content content, Fluid type, long maxAmount) {
            return LbaCompatUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
        }

        @Override
        public TriState query(Query query) {
            return TriStateUtilities.orGet(super.query(query), () -> {
                if (query instanceof TransferQuery)
                    return TriState.TRUE;
                return TriState.DEFAULT;
            });
        }
    }
}
