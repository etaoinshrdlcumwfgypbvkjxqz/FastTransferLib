package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.item.GroupedItemInvView;
import alexiil.mc.lib.attributes.item.ItemInvAmountChangeListener;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.view.flow.Subscription;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.view.flow.DisposableSubscriber;
import net.minecraft.item.ItemStack;

public class LbaGroupedItemListenerToSubscriber
        extends DisposableSubscriber<TransferData>
        implements ListenerToken {
    private final GroupedItemInvView owner;
    private final ItemInvAmountChangeListener listener;
    private final ListenerRemovalToken removalToken;

    protected LbaGroupedItemListenerToSubscriber(GroupedItemInvView owner,
                                                 ItemInvAmountChangeListener listener,
                                                 ListenerRemovalToken removalToken) {
        this.owner = owner;
        this.listener = listener;
        this.removalToken = removalToken;
    }

    public static LbaGroupedItemListenerToSubscriber of(GroupedItemInvView owner,
                                                        ItemInvAmountChangeListener listener,
                                                        ListenerRemovalToken removalToken) {
        return new LbaGroupedItemListenerToSubscriber(owner, listener, removalToken);
    }

    @Override
    protected void onSubscribe() {
        getSubscription().request(Subscription.UNBOUNDED_REQUEST);
    }

    @Override
    public void onNext(TransferData data) {
        ItemStack stack = ItemContent.asStack(data.getContent(), 1);
        GroupedItemInvView inventory = getOwner();
        int current = inventory.getAmount(stack);
        int previous = Math.max(0, Ints.saturatedCast(current - data.getType().applyToView(data.getAmount())));
        getListener().onChange(inventory, stack, previous, current);
    }

    @Override
    public void onError(Throwable throwable) {
        getRemovalToken().onListenerRemoved();
    }

    @Override
    public void onComplete() {
        getRemovalToken().onListenerRemoved();
    }

    protected ItemInvAmountChangeListener getListener() {
        return listener;
    }

    protected GroupedItemInvView getOwner() {
        return owner;
    }

    protected ListenerRemovalToken getRemovalToken() {
        return removalToken;
    }

    @Override
    public void removeListener() {
        cancel();
    }
}
