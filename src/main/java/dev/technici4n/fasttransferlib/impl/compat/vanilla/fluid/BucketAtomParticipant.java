package dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid;

import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.event.CapacityChangeEvent;
import dev.technici4n.fasttransferlib.api.view.event.NetTransferEvent;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.mixin.BucketItemAccess;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class BucketAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid> {
    private static final Set<Class<?>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(CapacityChangeEvent.class);
    private static final Set<Class<?>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(NetTransferEvent.class, CapacityChangeEvent.class);
    private final ItemConvertible item;
    private final ItemLookupContext lookupContext;

    public BucketAtomParticipant(@SuppressWarnings("unused") ItemConvertible item, ItemLookupContext lookupContext) {
        super(Fluid.class);
        this.item = item;
        this.lookupContext = lookupContext;
    }

    protected Fluid getFluid() {
        Item item = getItem().asItem();
        if (!(item instanceof BucketItem)) return Fluids.EMPTY;
        return ((BucketItemAccess) item).getFluid();
    }

    @Override
    protected long insertNew(Context context, Content content, Fluid type, long maxQuantity) {
        if (maxQuantity < FluidConstants.BUCKET
                || !getLookupContext().transform(context, 1L, ItemContent.of(type.getBucketItem()), 1L))
            return maxQuantity;
        return maxQuantity - FluidConstants.BUCKET;
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        // already filled
        return maxQuantity;
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        if (maxQuantity < FluidConstants.BUCKET
                || !getLookupContext().transform(context, 1L, ItemContent.of(Items.BUCKET), 1L))
            return 0L;
        return FluidConstants.BUCKET;
    }

    protected ItemConvertible getItem() {
        return item;
    }

    protected ItemLookupContext getLookupContext() {
        return lookupContext;
    }

    @Override
    public Content getContent() {
        return FluidContent.of(getFluid());
    }

    @Override
    public long getQuantity() {
        return getContent().isEmpty() ? 0L : FluidConstants.BUCKET;
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(FluidConstants.BUCKET);
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPushEvents() {
        // capacity is fixed, supports effectively
        return SUPPORTED_PUSH_EVENTS; // item context
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPullEvents() {
        return SUPPORTED_PULL_EVENTS; // item context
    }

    @Override
    public Object getRevisionFor(Class<?> event) {
        if (event == NetTransferEvent.class)
            return getContent(); // net change requires net content change
        return super.getRevisionFor(event);
    }

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
                    if (query instanceof TransferQuery)
                        return TriState.TRUE;
                    if (query instanceof StoreQuery)
                        return TriState.TRUE;
                    return TriState.DEFAULT;
                });
    }
}
