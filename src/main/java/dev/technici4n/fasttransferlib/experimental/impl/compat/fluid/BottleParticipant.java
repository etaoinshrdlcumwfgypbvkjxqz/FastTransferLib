package dev.technici4n.fasttransferlib.experimental.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Instance;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.impl.instance.ItemInstance;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.participant.SingleCategoryParticipant;
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
    protected long insert(Context context, Instance instance, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return maxAmount;
        if (PotionUtil.getPotion(lookupContext.getTag()) != Potions.EMPTY) return maxAmount;
        if (maxAmount < FluidConstants.BOTTLE) return maxAmount;
        if (type != Fluids.WATER) return maxAmount;
        if (!lookupContext.set(context, ItemInstance.of(Items.POTION), 1L)) return maxAmount;
        return maxAmount - FluidConstants.BOTTLE;
    }

    @Override
    protected long extract(Context context, Instance instance, Fluid type, long maxAmount) {
        if (lookupContext.getCount() == 0) return 0;
        if (PotionUtil.getPotion(lookupContext.getTag()) != Potions.WATER) return 0;
        if (maxAmount < FluidConstants.BOTTLE) return 0;
        if (!lookupContext.set(context, ItemInstance.of(Items.GLASS_BOTTLE), 1L)) return 0;
        return FluidConstants.BOTTLE;
    }
}
