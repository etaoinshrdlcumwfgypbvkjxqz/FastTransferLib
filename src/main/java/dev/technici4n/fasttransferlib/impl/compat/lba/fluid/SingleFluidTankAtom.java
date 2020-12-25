package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.fluid.SingleFluidTankView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import net.minecraft.fluid.Fluid;
import sun.misc.Cleaner;

import java.util.OptionalLong;

public class SingleFluidTankAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private final SingleFluidTankView delegate;
    private boolean hasListener;

    protected SingleFluidTankAtom(SingleFluidTankView delegate) {
        super(Fluid.class);
        this.delegate = delegate;

        OptionalWeakReference<SingleFluidTankAtom> weakThis = OptionalWeakReference.of(this);
        @SuppressWarnings("Convert2MethodRef")
        ListenerToken listenerToken = this.delegate.addListener((inv, tank, previous, current) -> weakThis.getOptional()
                        .ifPresent(this1 -> TransferUtilities.compileToTransferData(
                                LbaCompatUtil.asFluidContent(previous), LbaCompatUtil.asBigAmount(previous),
                                LbaCompatUtil.asFluidContent(current), LbaCompatUtil.asBigAmount(current)
                        ).forEachRemaining(data -> this1.reviseAndNotify(data) /* todo javac bug */)),
                () -> weakThis.getOptional()
                        .ifPresent(this1 -> this1.setHasListener(false)));

        if (listenerToken == null) {
            this.hasListener = false;
        } else {
            this.hasListener = true;
            Cleaner.create(this, listenerToken::removeListener);
        }
    }

    public static SingleFluidTankAtom of(SingleFluidTankView delegate) {
        return new SingleFluidTankAtom(delegate);
    }

    @Override
    public Content getContent() {
        return LbaCompatUtil.asFluidContent(getDelegate().get());
    }

    @Override
    public long getAmount() {
        return LbaCompatUtil.asAmount(getDelegate().get());
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(LbaCompatUtil.asAmount(getDelegate().getMaxAmount_F()));
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

    protected SingleFluidTankView getDelegate() {
        return delegate;
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
