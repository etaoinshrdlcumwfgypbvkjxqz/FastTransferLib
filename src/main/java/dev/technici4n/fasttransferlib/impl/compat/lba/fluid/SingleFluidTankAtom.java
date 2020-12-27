package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.fluid.SingleFluidTankView;
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
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.fluid.Fluid;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class SingleFluidTankAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private static final Set<Class<? extends PushEvent>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(TransferEvent.class);
    private static final Set<Class<? extends PullEvent>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, TransferNetEvent.class);
    private final SingleFluidTankView delegate;
    private boolean hasTransferListener;

    protected SingleFluidTankAtom(SingleFluidTankView delegate) {
        super(Fluid.class);
        this.delegate = delegate;

        OptionalWeakReference<SingleFluidTankAtom> weakThis = OptionalWeakReference.of(this);
        ListenerToken listenerToken = this.delegate.addListener((inv, tank, previous, current) -> weakThis.getOptional()
                        .ifPresent(this1 -> TransferUtilities.compileToTransferData(
                                LbaCompatUtil.asFluidContent(previous), LbaCompatUtil.asBigQuantity(previous),
                                LbaCompatUtil.asFluidContent(current), LbaCompatUtil.asBigQuantity(current)
                        ).forEachRemaining(data -> {
                            this1.revise(TransferNetEvent.class);
                            this1.reviseAndNotify(TransferEvent.class, data);
                        })),
                () -> weakThis.getOptional().ifPresent(SingleFluidTankAtom::onListenerRemoved));

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

    public static SingleFluidTankAtom of(SingleFluidTankView delegate) {
        return new SingleFluidTankAtom(delegate);
    }

    @Override
    public Content getContent() {
        return LbaCompatUtil.asFluidContent(getDelegate().get());
    }

    @Override
    public long getQuantity() {
        return LbaCompatUtil.asQuantity(getDelegate().get());
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(LbaCompatUtil.asQuantity(getDelegate().getMaxAmount_F()));
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, getContent(), maxQuantity);
    }

    @Override
    protected long insertNew(Context context, Content content, Fluid type, long maxQuantity) {
        return LbaCompatUtil.genericInsertImpl(getDelegate(), context, content, maxQuantity);
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        return LbaCompatUtil.genericExtractImpl(getDelegate(), context, getContent(), maxQuantity);
    }

    protected SingleFluidTankView getDelegate() {
        return delegate;
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

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
            if (query instanceof ContentQuery
                    && !getDelegate().isValid(LbaCompatUtil.asFluidKey(((ContentQuery) query).getContent())))
                return TriState.FALSE;
            if (query instanceof TransferQuery)
                return TriState.TRUE;
            if (query instanceof StoreQuery)
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
    }
}
