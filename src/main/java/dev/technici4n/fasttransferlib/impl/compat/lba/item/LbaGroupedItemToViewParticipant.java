package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.GroupedItemInvView;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.api.view.model.MapModel;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.impl.base.AbstractComposedViewParticipant;
import dev.technici4n.fasttransferlib.impl.base.transfer.AbstractMonoCategoryParticipant;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractMonoCategoryView;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import dev.technici4n.fasttransferlib.impl.view.flow.TransferDataImpl;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.item.Item;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LbaGroupedItemToViewParticipant
        extends AbstractComposedViewParticipant
        implements MapModel {
    private static final Set<Class<?>> SUPPORTED_PUSH_NOTIFICATIONS = ImmutableSet.of(TransferData.class);
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

    @SuppressWarnings("unchecked")
    @Override
    public Map<Content, ? extends Atom> getAtomMap() {
        GroupedItemInvView delegate = getDelegate();
        return Maps.toMap((Iterable<Content>) getContents(), content -> {
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
        private boolean hasListener;

        protected ViewImpl(GroupedItemInvView delegate) {
            super(Item.class);

            OptionalWeakReference<ViewImpl> weakThis = OptionalWeakReference.of(this);
            ListenerToken listenerToken = delegate.addListener((inv, item, previous, current) -> weakThis.getOptional()
                            .ifPresent(this1 -> {
                                Content content1 = ItemContent.of(item);

                                int diff = current - previous;
                                if (diff == 0)
                                    return;

                                this1.reviseAndNotify(TransferData.class,
                                        TransferDataImpl.of(TransferAction.fromDifference(diff > 0), content1, Math.abs(diff)));
                            }),
                    () -> weakThis.getOptional().ifPresent(ViewImpl::onListenerRemoved));

            if (listenerToken == null) {
                this.hasListener = false;
            } else {
                this.hasListener = true;
                Cleaner.create(this, listenerToken::removeListener);
            }
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
            return LbaGroupedItemToViewParticipant.this;
        }

        @Override
        protected Collection<? extends Class<?>> getSupportedPushNotifications() {
            return isHasListener() ? SUPPORTED_PUSH_NOTIFICATIONS : ImmutableSet.of();
        }

        @Override
        protected boolean supportsPullNotification() {
            return isHasListener();
        }

        protected boolean isHasListener() {
            return hasListener;
        }

        protected void setHasListener(@SuppressWarnings("SameParameterValue") boolean hasListener) {
            this.hasListener = hasListener;
        }

        protected void onListenerRemoved() {
            setHasListener(false);
            getPublisherIfPresent(TransferData.class).ifPresent(EmittingPublisher::clearSubscribers);
        }
    }

    public class ParticipantImpl
            extends AbstractMonoCategoryParticipant<Item> {
        protected ParticipantImpl() {
            super(Item.class);
        }

        @Override
        protected long insertMono(Context context, Content content, Item type, long maxAmount) {
            return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
        }

        @Override
        protected long extractMono(Context context, Content content, Item type, long maxAmount) {
            return LbaCompatUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
        }
    }
}
