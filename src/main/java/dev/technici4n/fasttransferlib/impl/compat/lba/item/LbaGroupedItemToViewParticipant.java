package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.GroupedItemInvView;
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
import dev.technici4n.fasttransferlib.api.view.event.PullEvent;
import dev.technici4n.fasttransferlib.api.view.event.PushEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferNetEvent;
import dev.technici4n.fasttransferlib.api.view.model.MapModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractComposedViewParticipant;
import dev.technici4n.fasttransferlib.impl.base.transfer.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractMonoCategoryView;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.view.event.TransferEventImpl;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LbaGroupedItemToViewParticipant
        extends AbstractComposedViewParticipant
        implements MapModel {
    private static final Set<Class<? extends PushEvent>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(TransferEvent.class);
    private static final Set<Class<? extends PullEvent>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, TransferNetEvent.class);
    private final GroupedItemInvView delegate;
    private final View view;
    private final Participant participant;

    protected LbaGroupedItemToViewParticipant(GroupedItemInvView delegate) {
        this.delegate = delegate;

        this.view = new ViewImpl(this.delegate);
        this.participant = new ParticipantImpl();
    }

    public static LbaGroupedItemToViewParticipant of(GroupedItemInvView delegate) {
        return new LbaGroupedItemToViewParticipant(delegate);
    }

    @Override
    public Map<Content, Atom> getAtomMap() {
        GroupedItemInvView delegate = getDelegate();
        return Maps.toMap(getContents(), content -> {
            assert content != null;
            return MonoGroupedItemInvAtom.of(delegate, content);
        });
    }

    protected GroupedItemInvView getDelegate() {
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

    public class ViewImpl
            extends AbstractMonoCategoryView<Item> {
        private boolean hasTransferListener;

        protected ViewImpl(GroupedItemInvView delegate) {
            super(Item.class);

            OptionalWeakReference<ViewImpl> weakThis = OptionalWeakReference.of(this);
            ListenerToken listenerToken = delegate.addListener((inv, item, previous, current) -> weakThis.getOptional()
                            .ifPresent(this1 -> {
                                Content content1 = ItemContent.of(item);

                                int diff = current - previous;
                                if (diff == 0)
                                    return;

                                this1.revise(TransferNetEvent.class);
                                this1.reviseAndNotify(TransferEvent.class,
                                        TransferEventImpl.of(TransferAction.fromDifference(diff > 0), content1, Math.abs(diff)));
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
        public @NotNull Iterator<Atom> iterator() {
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
        protected long getQuantity(Content content, Item type) {
            return getDelegate().getAmount(ItemContent.asStack(content, 1));
        }

        @Override
        public Object2LongMap<Content> getQuantitys() {
            return Object2LongMaps.unmodifiable(
                    new Object2LongOpenHashMap<>(Maps.toMap(getContents(), this::getQuantity))
            );
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public Set<Content> getContents() {
            return getDelegate().getStoredStacks().stream()
                    .map(ItemContent::of)
                    .collect(ImmutableSet.toImmutableSet());
        }

        @Override
        public Model getDirectModel() {
            return LbaGroupedItemToViewParticipant.this;
        }

        @Override
        protected Collection<? extends Class<? extends PushEvent>> getSupportedPushEvents() {
            return isHasTransferListener() ? SUPPORTED_PUSH_EVENTS : ImmutableSet.of();
        }

        @Override
        protected Collection<? extends Class<? extends PullEvent>> getSupportedPullEvents() {
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
            extends AbstractMonoCategoryParticipant<Item> {
        protected ParticipantImpl() {
            super(Item.class);
        }

        @Override
        protected long insertMono(Context context, Content content, Item type, long maxQuantity) {
            return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxQuantity);
        }

        @Override
        protected long extractMono(Context context, Content content, Item type, long maxQuantity) {
            return LbaCompatUtil.genericExtractImpl(getDelegate(), context, content, maxQuantity);
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
