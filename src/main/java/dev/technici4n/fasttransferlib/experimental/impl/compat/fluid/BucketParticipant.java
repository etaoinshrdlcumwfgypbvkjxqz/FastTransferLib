package dev.technici4n.fasttransferlib.experimental.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Instance;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.impl.instance.ItemInstance;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.participant.SingleCategoryParticipant;
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
    protected long insert(Context context, Instance instance, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return maxAmount;
        if (!(item.asItem() instanceof BucketItem)) return maxAmount;
        if (getFluid() != Fluids.EMPTY) return maxAmount;
        if (maxAmount < FluidConstants.BUCKET) return maxAmount;
        if (!lookupContext.set(context, ItemInstance.of(type.getBucketItem()), 1L)) return maxAmount;
        return maxAmount - FluidConstants.BUCKET;
    }

    @Override
    protected long extract(Context context, Instance instance, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return 0;
        if (getFluid() == Fluids.EMPTY || getFluid() != type) return 0;
        if (!lookupContext.set(context, ItemInstance.of(Items.BUCKET), 1L)) return 0;
        return FluidConstants.BUCKET;
    }
}
