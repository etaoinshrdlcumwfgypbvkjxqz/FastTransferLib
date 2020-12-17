package dev.technici4n.fasttransferlib.experimental.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.experimental.impl.view.AbstractMonoCategoryAtom;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

// TODO improvement
public class BottleAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid> {
    private final ItemLookupContext lookupContext;

    public BottleAtomParticipant(@SuppressWarnings("unused") ItemConvertible item, ItemLookupContext lookupContext) {
        super(Fluid.class);
        this.lookupContext = lookupContext;
    }

    protected Fluid getFluid() {
        return PotionUtil.getPotion(getLookupContext().getTag()) == Potions.WATER ? Fluids.WATER : Fluids.EMPTY;
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        ItemLookupContext lookupContext = getLookupContext();
        if (lookupContext.getCount() == 0) return maxAmount;
        if (PotionUtil.getPotion(lookupContext.getTag()) != Potions.EMPTY) return maxAmount;
        if (maxAmount < FluidConstants.BOTTLE) return maxAmount;
        if (type != Fluids.WATER) return maxAmount;
        if (!lookupContext.set(context, ItemContent.of(Items.POTION), 1L)) return maxAmount;
        return maxAmount - FluidConstants.BOTTLE;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        ItemLookupContext lookupContext = getLookupContext();
        if (lookupContext.getCount() == 0) return 0;
        if (PotionUtil.getPotion(lookupContext.getTag()) != Potions.WATER) return 0;
        if (maxAmount < FluidConstants.BOTTLE) return 0;
        if (!lookupContext.set(context, ItemContent.of(Items.GLASS_BOTTLE), 1L)) return 0;
        return FluidConstants.BOTTLE;
    }

    protected ItemLookupContext getLookupContext() {
        return lookupContext;
    }

    @Override
    public Content getContent() {
        return PotionUtil.getPotion(lookupContext.getTag()) == Potions.WATER
                ? FluidContent.of(Fluids.WATER)
                : EmptyContent.INSTANCE;
    }

    @Override
    public long getAmount() {
        return getContent().isEmpty() ? 0L : FluidConstants.BOTTLE;
    }
}
