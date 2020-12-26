package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
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
import dev.technici4n.fasttransferlib.impl.util.OptionalWeakReference;
import dev.technici4n.fasttransferlib.impl.util.TransferUtilities;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.view.event.TransferEventImpl;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.fluid.Fluid;
import sun.misc.Cleaner;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class MonoGroupedFluidInvAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private static final Set<Class<?>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(TransferEvent.class);
    private static final Set<Class<?>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, NetTransferEvent.class);
    private final GroupedFluidInvView delegate;
    private final Content content;
    private final FluidKey key;
    private boolean hasTransferListener;

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

                            TransferAction action = TransferAction.fromDifference(diff.isPositive());
                            TransferUtilities.BigIntegerAsLongIterator.ofStream(LbaCompatUtil.asBigAmount(LbaCompatUtil.abs(diff)))
                                    .mapToObj(diff1 -> TransferEventImpl.of(action, content1, diff1))
                                    .forEach(data -> {
                                        this1.revise(NetTransferEvent.class);
                                        this1.reviseAndNotify(TransferEvent.class, data);
                                    });
                        }),
                () -> weakThis.getOptional().ifPresent(MonoGroupedFluidInvAtom::onListenerRemoved));

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
        return content; // locked
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
        return maxAmount; // reject
    }

    @Override
    protected long extractCurrent(Context context, long maxAmount) {
        return LbaCompatUtil.genericExtractImpl(getDelegate(), context, getContent(), maxAmount);
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
