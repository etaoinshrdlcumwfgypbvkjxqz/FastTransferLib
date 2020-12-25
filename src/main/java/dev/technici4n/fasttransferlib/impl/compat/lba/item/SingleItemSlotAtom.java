package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.SingleItemSlotView;
import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import net.minecraft.item.Item;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.OptionalLong;

public class SingleItemSlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final SingleItemSlotView delegate;
    private boolean hasListener;

    protected SingleItemSlotAtom(SingleItemSlotView delegate) {
        super(Item.class);
        this.delegate = delegate;

        OptionalWeakReference<SingleItemSlotAtom> weakThis = OptionalWeakReference.of(this);
        ListenerToken listenerToken = this.delegate.getBackingInv().addListener(
                inv -> weakThis.getOptional().ifPresent(SingleItemSlotAtom::revise),
                () -> weakThis.getOptional().ifPresent(SingleItemSlotAtom::onListenerRemoved));

        if (listenerToken == null) {
            this.hasListener = false;
        } else {
            this.hasListener = true;
            Cleaner.create(this, listenerToken::removeListener);
        }
    }

    protected void onListenerRemoved() {
        setHasListener(false);
        getPublisherIfPresent(TransferData.class).ifPresent(EmittingPublisher::clearSubscribers);
    }

    public static SingleItemSlotAtom of(SingleItemSlotView delegate) {
        return new SingleItemSlotAtom(delegate);
    }

    @Override
    public Content getContent() {
        return ItemContent.of(getDelegate().get());
    }

    @Override
    public long getAmount() {
        return getDelegate().get().getCount();
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(getDelegate().getMaxAmount(getDelegate().get()));
    }

    @Override
    protected long extractCurrent(Context context, long maxAmount) {
        return LbaCompatUtil.genericExtractImpl(getDelegate(), context, getContent(), maxAmount);
    }

    @Override
    protected long insertCurrent(Context context, long maxAmount) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, getContent(), maxAmount);
    }

    @Override
    protected long insertNew(Context context, Content content, Item type, long maxAmount) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
    }

    protected SingleItemSlotView getDelegate() {
        return delegate;
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPushNotifications() {
        return ImmutableSet.of(); // mark dirty listener only
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
}
