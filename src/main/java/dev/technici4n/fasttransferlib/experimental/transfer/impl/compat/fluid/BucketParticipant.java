package dev.technici4n.fasttransferlib.experimental.transfer.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Transferable;
import dev.technici4n.fasttransferlib.experimental.transfer.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.participant.SingleCategoryParticipant;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.transferable.ItemTransferable;
import dev.technici4n.fasttransferlib.impl.mixin.BucketItemAccess;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

public class BucketParticipant
        extends SingleCategoryParticipant<Fluid> {
    private final ItemConvertible item;
    private final ItemLookupContext lookupContext;

    public BucketParticipant(@SuppressWarnings("unused") ItemConvertible item, ItemLookupContext lookupContext) {
        super(Fluid.class);
        this.item = item;
        this.lookupContext = lookupContext;
    }

    protected Fluid getFluid() {
        if (!(item.asItem() instanceof BucketItem)) return Fluids.EMPTY;
        return ((BucketItemAccess) item.asItem()).getFluid();
    }

    @Override
    protected long insert(Context context, Transferable transferable, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return maxAmount;
        if (!(item.asItem() instanceof BucketItem)) return maxAmount;
        if (getFluid() != Fluids.EMPTY) return maxAmount;
        if (maxAmount < FluidConstants.BUCKET) return maxAmount;
        if (!lookupContext.set(context, ItemTransferable.of(type.getBucketItem()), 1L)) return maxAmount;
        return maxAmount - FluidConstants.BUCKET;
    }

    @Override
    protected long extract(Context context, Transferable transferable, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return 0;
        if (getFluid() == Fluids.EMPTY || getFluid() != type) return 0;
        if (!lookupContext.set(context, ItemTransferable.of(Items.BUCKET), 1L)) return 0;
        return FluidConstants.BUCKET;
    }
}
