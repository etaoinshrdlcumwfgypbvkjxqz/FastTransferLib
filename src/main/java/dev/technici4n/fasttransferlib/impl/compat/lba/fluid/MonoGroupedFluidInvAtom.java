package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import dev.technici4n.fasttransferlib.impl.view.observer.TransferDataImpl;
import net.minecraft.fluid.Fluid;
import sun.misc.Cleaner;

import java.util.OptionalLong;

public class MonoGroupedFluidInvAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private final GroupedFluidInvView delegate;
    private final Content content;
    private final FluidKey key;
    private boolean hasListener;

    @SuppressWarnings("Convert2MethodRef")
    protected MonoGroupedFluidInvAtom(GroupedFluidInvView delegate, Content content) {
        super(Fluid.class);
        assert content.getCategory() == Fluid.class;

        this.delegate = delegate;
        this.content = content;
        this.key = FluidKeys.get((Fluid) content.getType());

        OptionalWeakReference<MonoGroupedFluidInvAtom> weakThis = OptionalWeakReference.of(this);
        ListenerToken listenerToken = this.delegate.addListener_F((inv, fluid, previous, current) -> weakThis.getOptional()
                        .ifPresent(this1 -> {
                            Content content1 = LbaCompatUtil.asFluidContent(fluid);
                            if (!content1.equals(this1.getContent()))
                                return;

                            FluidAmount diff = current.sub(previous);
                            if (diff.isZero())
                                return;

                            TransferData.Type type = TransferData.Type.fromDifference(diff.isPositive());
                            TransferUtilities.BigIntegerAsLongIterator.ofStream(LbaCompatUtil.asBigAmount(LbaCompatUtil.abs(diff)))
                                    .mapToObj(diff1 -> TransferDataImpl.of(type, content1, diff1))
                                    .forEach(data -> this1.reviseAndNotify(data)); // todo javac bug
                        }),
                () -> weakThis.getOptional().ifPresent(MonoGroupedFluidInvAtom::onListenerRemoved));

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

    public static MonoGroupedFluidInvAtom of(GroupedFluidInvView delegate, Content content) {
        return new MonoGroupedFluidInvAtom(delegate, content);
    }

    protected GroupedFluidInvView getDelegate() {
        return delegate;
    }

    protected FluidKey getKey() {
        return key;
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public long getAmount() {
        return LbaCompatUtil.asAmount(getDelegate().getAmount_F(getKey()));
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(LbaCompatUtil.asAmount(getDelegate().getCapacity_F(getKey())));
    }

    @Override
    protected long insertCurrent(Context context, long maxAmount) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, getContent(), maxAmount);
    }

    @Override
    protected long insertNew(Context context, Content content, Fluid type, long maxAmount) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extractCurrent(Context context, long maxAmount) {
        return LbaCompatUtil.genericExtractImpl(getDelegate(), context, getContent(), maxAmount);
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
