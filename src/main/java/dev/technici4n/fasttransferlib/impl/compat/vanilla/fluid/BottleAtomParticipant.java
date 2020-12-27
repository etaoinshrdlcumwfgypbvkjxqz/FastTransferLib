package dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid;

import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.api.query.ContentQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.event.*;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

public class BottleAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid> {
    private static final Set<Class<? extends PushEvent>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(CapacityChangeEvent.class);
    private static final Set<Class<? extends PullEvent>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferNetEvent.class, CapacityChangeEvent.class, CapacityChangeNetEvent.class);
    private final ItemLookupContext lookupContext;

    public BottleAtomParticipant(ItemLookupContext lookupContext) {
        super(Fluid.class);
        this.lookupContext = lookupContext;
    }

    protected Fluid getFluid() {
        return PotionUtil.getPotion(getLookupContext().getData()) == Potions.WATER ? Fluids.WATER : Fluids.EMPTY;
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
        return getContent().isEmpty() ? 0L : FluidConstants.BOTTLE;
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(FluidConstants.BOTTLE);
    }

    protected static Optional<? extends Content> getFluidContentAsItemContent(Content fluidContent) {
        if (fluidContent.equals(FluidContent.of(Fluids.WATER)))
            return Optional.of(ItemContent.of(Items.POTION));

        // for mixins, should be a better way to do this

        return Optional.empty();
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        if (maxQuantity < FluidConstants.BOTTLE
                || !getLookupContext().transform(context, 1L, ItemContent.of(Items.GLASS_BOTTLE), 1L))
            return 0L;
        return FluidConstants.BOTTLE;
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        // already filled
        return maxQuantity;
    }

    @Override
    protected long insertNew(Context context, Content content, Fluid type, long maxQuantity) {
        if (maxQuantity < FluidConstants.BOTTLE
                || !getFluidContentAsItemContent(content)
                .filter(itemContent -> getLookupContext().transform(context, 1L, itemContent, 1L))
                .isPresent())
            return maxQuantity;
        return maxQuantity - FluidConstants.BOTTLE;
    }

    @Override
    protected Collection<? extends Class<? extends PushEvent>> getSupportedPushEvents() {
        // capacity is fixed, therefore supports it effectively
        return SUPPORTED_PUSH_EVENTS; // item context
    }

    @Override
    protected Collection<? extends Class<? extends PullEvent>> getSupportedPullEvents() {
        return SUPPORTED_PULL_EVENTS; // item context
    }

    @Override
    public Object getRevisionFor(Class<? extends PullEvent> event) {
        if (event == TransferNetEvent.class)
            return getContent(); // net change involves net content change
        return super.getRevisionFor(event);
    }

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
                    if (query instanceof ContentQuery
                            && !getFluidContentAsItemContent(((ContentQuery) query).getContent()).isPresent())
                        return TriState.FALSE;
                    if (query instanceof TransferQuery)
                        return TriState.TRUE;
                    if (query instanceof StoreQuery)
                        return TriState.TRUE;
                    return TriState.DEFAULT;
                });
    }
}
