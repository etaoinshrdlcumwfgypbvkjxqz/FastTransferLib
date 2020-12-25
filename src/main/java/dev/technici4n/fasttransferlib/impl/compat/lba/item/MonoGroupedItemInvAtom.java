package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.GroupedItemInvView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.view.observer.TransferDataImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import sun.misc.Cleaner;

import java.util.OptionalLong;

public class MonoGroupedItemInvAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final GroupedItemInvView delegate;
    private final Content content;
    private final ItemStack key;
    private boolean hasListener;

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

                            this1.reviseAndNotify(TransferDataImpl.of(TransferData.Type.fromDifference(diff > 0), content1, Math.abs(diff)));
                        }),
                () -> weakThis.getOptional().ifPresent(MonoGroupedItemInvAtom::onListenerRemoved));

        if (listenerToken == null) {
            this.hasListener = false;
        } else {
            this.hasListener = true;
            Cleaner.create(this, listenerToken::removeListener);
        }
    }

    protected void onListenerRemoved() {
        setHasListener(false);
        clearSubscribers();
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
        return content;
    }

    @Override
    public long getAmount() {
        return getDelegate().getAmount(getKey());
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(getDelegate().getCapacity(getKey()));
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

    @Override
    protected boolean supportsPushNotification() {
        return isHasListener();
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
