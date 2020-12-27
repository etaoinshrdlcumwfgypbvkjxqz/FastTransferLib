package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.SingleItemSlotView;
import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.ContentQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.event.PullEvent;
import dev.technici4n.fasttransferlib.api.view.event.PushEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferNetEvent;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.Item;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class SingleItemSlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private static final Set<Class<? extends PullEvent>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, TransferNetEvent.class);
    private final SingleItemSlotView delegate;
    private boolean hasTransferListener;

    protected SingleItemSlotAtom(SingleItemSlotView delegate) {
        super(Item.class);
        this.delegate = delegate;

        OptionalWeakReference<SingleItemSlotAtom> weakThis = OptionalWeakReference.of(this);
        ListenerToken listenerToken = this.delegate.getBackingInv().addListener(
                inv -> weakThis.getOptional().ifPresent(this1 -> {
                    this1.revise(TransferNetEvent.class);
                    this1.revise(TransferEvent.class);
                }),
                () -> weakThis.getOptional().ifPresent(SingleItemSlotAtom::onListenerRemoved));

        if (listenerToken == null) {
            this.hasTransferListener = false;
        } else {
            this.hasTransferListener = true;
            Cleaner.create(this, listenerToken::removeListener);
        }
    }

    protected void onListenerRemoved() {
        setHasTransferListener(false);
        getPublisherIfPresent(TransferEvent.class).ifPresent(EmittingPublisher::clearSubscribers);
    }

    public static SingleItemSlotAtom of(SingleItemSlotView delegate) {
        return new SingleItemSlotAtom(delegate);
    }

    @Override
    public Content getContent() {
        return ItemContent.of(getDelegate().get());
    }

    @Override
    public long getQuantity() {
        return getDelegate().get().getCount();
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(getDelegate().getMaxAmount(getDelegate().get()));
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        return LbaCompatUtil.genericExtractImpl(getDelegate(), context, getContent(), maxQuantity);
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, getContent(), maxQuantity);
    }

    @Override
    protected long insertNew(Context context, Content content, Item type, long maxQuantity) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxQuantity);
    }

    protected SingleItemSlotView getDelegate() {
        return delegate;
    }

    @Override
    protected Collection<? extends Class<? extends PushEvent>> getSupportedPushEvents() {
        return ImmutableSet.of(); // mark dirty listener only
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

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
            if (query instanceof ContentQuery
                    && !getDelegate().isValid(ItemContent.asStack(((ContentQuery) query).getContent(), 1)))
                return TriState.FALSE;
            if (query instanceof TransferQuery)
                return TriState.TRUE;
            if (query instanceof StoreQuery)
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
    }
}
