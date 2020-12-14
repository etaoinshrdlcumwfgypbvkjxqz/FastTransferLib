package dev.technici4n.fasttransferlib.experimental.transfer.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Transferable;
import dev.technici4n.fasttransferlib.experimental.transfer.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.participant.SingleCategoryParticipant;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.transferable.ItemTransferable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

public class BottleParticipant
        extends SingleCategoryParticipant<Fluid> {
    private final ItemLookupContext lookupContext;

    public BottleParticipant(@SuppressWarnings("unused") ItemConvertible item, ItemLookupContext lookupContext) {
        super(Fluid.class);
        this.lookupContext = lookupContext;
    }

    protected Fluid getFluid() {
        return PotionUtil.getPotion(lookupContext.getTag()) == Potions.WATER ? Fluids.WATER : Fluids.EMPTY;
    }

    @Override
    protected long insert(Context context, Transferable transferable, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return maxAmount;
        if (PotionUtil.getPotion(lookupContext.getTag()) != Potions.EMPTY) return maxAmount;
        if (maxAmount < FluidConstants.BOTTLE) return maxAmount;
        if (type != Fluids.WATER) return maxAmount;
        if (!lookupContext.set(context, ItemTransferable.of(Items.POTION), 1L)) return maxAmount;
        return maxAmount - FluidConstants.BOTTLE;
    }

    @Override
    protected long extract(Context context, Transferable transferable, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return 0;
        if (PotionUtil.getPotion(lookupContext.getTag()) != Potions.WATER) return 0;
        if (maxAmount < FluidConstants.BOTTLE) return 0;
        if (!lookupContext.set(context, ItemTransferable.of(Items.GLASS_BOTTLE), 1L)) return 0;
        return FluidConstants.BOTTLE;
    }
}
