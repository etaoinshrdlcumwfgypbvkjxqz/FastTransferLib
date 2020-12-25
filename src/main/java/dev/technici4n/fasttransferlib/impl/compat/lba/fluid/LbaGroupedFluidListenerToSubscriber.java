package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.fluid.FluidInvAmountChangeListener_F;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import dev.technici4n.fasttransferlib.api.view.flow.Subscription;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.view.flow.DisposableSubscriber;

public class LbaGroupedFluidListenerToSubscriber
        extends DisposableSubscriber<TransferData>
        implements ListenerToken {
    private final GroupedFluidInvView owner;
    private final FluidInvAmountChangeListener_F listener;
    private final ListenerRemovalToken removalToken;

    protected LbaGroupedFluidListenerToSubscriber(GroupedFluidInvView owner,
                                                  FluidInvAmountChangeListener_F listener,
                                                  ListenerRemovalToken removalToken) {
        this.owner = owner;
        this.listener = listener;
        this.removalToken = removalToken;
    }

    public static LbaGroupedFluidListenerToSubscriber of(GroupedFluidInvView owner,
                                                         FluidInvAmountChangeListener_F listener,
                                                         ListenerRemovalToken removalToken) {
        return new LbaGroupedFluidListenerToSubscriber(owner, listener, removalToken);
    }

    @Override
    protected void onSubscribe() {
        getSubscription().request(Subscription.UNBOUNDED_REQUEST);
    }

    @Override
    public void onNext(TransferData data) {
        FluidKey fluid = LbaCompatUtil.asFluidKey(data.getContent());
        GroupedFluidInvView inventory = getOwner();
        FluidAmount current = inventory.getAmount_F(fluid);
        FluidAmount previous = current.sub(LbaCompatUtil.asFluidAmount(data.getType().applyToView(data.getAmount()))).max(FluidAmount.ZERO);
        getListener().onChange(inventory, fluid, previous, current);
    }

    @Override
    public void onError(Throwable throwable) {
        getRemovalToken().onListenerRemoved();
    }

    @Override
    public void onComplete() {
        getRemovalToken().onListenerRemoved();
    }

    protected FluidInvAmountChangeListener_F getListener() {
        return listener;
    }

    protected GroupedFluidInvView getOwner() {
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
