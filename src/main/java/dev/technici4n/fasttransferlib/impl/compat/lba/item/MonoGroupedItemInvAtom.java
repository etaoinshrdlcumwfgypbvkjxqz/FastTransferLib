package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.GroupedItemInvView;
import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.ContentQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.event.NetTransferEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.view.event.TransferEventImpl;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class MonoGroupedItemInvAtom
        extends AbstractMonoCategoryAtom<Item> {
    private static final Set<Class<?>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(TransferEvent.class);
    private static final Set<Class<?>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, NetTransferEvent.class);
    private final GroupedItemInvView delegate;
    private final Content content;
    private final ItemStack key;
    private boolean hasTransferListener;

    protected MonoGroupedItemInvAtom(GroupedItemInvView delegate, Content content) {
        super(Item.class);
        assert content.getCategory() == Item.class;

        this.delegate = delegate;
        this.content = content;
        this.key = ItemContent.asStack(this.content, 1);

        OptionalWeakReference<MonoGroupedItemInvAtom> weakThis = OptionalWeakReference.of(this);
        ListenerToken listenerToken = this.delegate.addListener((inv, item, previous, current) -> weakThis.getOptional()
                        .ifPresent(this1 -> {
                            Content content1 = ItemContent.of(item);
                            if (!content1.equals(this1.getContent()))
                                return;

                            int diff = current - previous;
                            if (diff == 0)
                                return;

                            this1.revise(NetTransferEvent.class);
                            this1.reviseAndNotify(TransferEvent.class,
                                    TransferEventImpl.of(TransferAction.fromDifference(diff > 0), content1, Math.abs(diff)));
                        }),
                () -> weakThis.getOptional().ifPresent(MonoGroupedItemInvAtom::onListenerRemoved));

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

    public static MonoGroupedItemInvAtom of(GroupedItemInvView delegate, Content content) {
        return new MonoGroupedItemInvAtom(delegate, content);
    }

    protected GroupedItemInvView getDelegate() {
        return delegate;
    }

    protected ItemStack getKey() {
        return key;
    }

    @Override
    public Content getContent() {
        return content; // locked
    }

    @Override
    public long getQuantity() {
        return getDelegate().getAmount(getKey());
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(getDelegate().getCapacity(getKey()));
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
        return maxQuantity; // reject, only this content
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

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
            if (query instanceof ContentQuery && !((ContentQuery) query).getContent().equals(getContent()))
                return TriState.FALSE;
            if (query instanceof TransferQuery)
                return TriState.TRUE;
            if (query instanceof StoreQuery)
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
    }
}
