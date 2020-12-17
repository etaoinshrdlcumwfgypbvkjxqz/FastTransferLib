package dev.technici4n.fasttransferlib.experimental.impl.compat.vanilla.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.experimental.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import java.util.Optional;

public class BottleAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid> {
    private final ItemLookupContext lookupContext;

    public BottleAtomParticipant(ItemLookupContext lookupContext) {
        super(Fluid.class);
        this.lookupContext = lookupContext;
    }

    protected Fluid getFluid() {
        return PotionUtil.getPotion(getLookupContext().getData()) == Potions.WATER ? Fluids.WATER : Fluids.EMPTY;
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        ItemLookupContext lookupContext = getLookupContext();
        if (maxAmount < FluidConstants.BOTTLE
                || !getContent().isEmpty()
                || !getFluidContentAsItemContent(content)
                .filter(itemContent -> lookupContext.transform(context, 1L, itemContent, 1L))
                .isPresent())
            return maxAmount;
        return maxAmount - FluidConstants.BOTTLE;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        ItemLookupContext lookupContext = getLookupContext();
        if (maxAmount < FluidConstants.BOTTLE
                || getContent().isEmpty()
                || !content.equals(getContent())
                || !lookupContext.transform(context, 1L, ItemContent.of(Items.GLASS_BOTTLE), 1L))
            return 0L;
        return FluidConstants.BOTTLE;
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
        return getContent().isEmpty() ? 0L : FluidConstants.BOTTLE;
    }

    protected static Optional<? extends Content> getFluidContentAsItemContent(Content fluidContent) {
        if (fluidContent.equals(FluidContent.of(Fluids.WATER)))
            return Optional.of(ItemContent.of(Items.POTION));

        // for mixins, should be a better way to do this

        return Optional.empty();
    }
}
