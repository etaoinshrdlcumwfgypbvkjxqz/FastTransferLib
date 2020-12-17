package dev.technici4n.fasttransferlib.experimental.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.experimental.impl.view.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.mixin.BucketItemAccess;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

public class BucketAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid> {
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
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        if (maxAmount < FluidConstants.BUCKET
                || !getContent().isEmpty()
                || !getLookupContext().transform(context, 1L, ItemContent.of(type.getBucketItem()), 1L))
            return maxAmount;
        return maxAmount - FluidConstants.BUCKET;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        if (maxAmount < FluidConstants.BUCKET
                || getContent().isEmpty()
                || !content.equals(getContent())
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
    public long getAmount() {
        return getContent().isEmpty() ? 0L : FluidConstants.BUCKET;
    }
}
