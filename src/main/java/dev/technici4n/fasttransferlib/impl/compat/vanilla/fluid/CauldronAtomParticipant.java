package dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid;

import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.ContentQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.event.*;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import dev.technici4n.fasttransferlib.impl.util.WorldUtilities;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class CauldronAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid>
        implements Atom {
    private static final Set<Class<? extends PushEvent>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(CapacityChangeEvent.class);
    private static final Set<Class<? extends PullEvent>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferNetEvent.class, CapacityChangeEvent.class, CapacityChangeNetEvent.class);
    private final WorldAccess world;
    private final BlockPos position;

    public CauldronAtomParticipant(WorldAccess world, BlockPos position) {
        super(Fluid.class);
        this.world = world;
        this.position = position.toImmutable();
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        WorldAccess world = getWorld();
        BlockPos position = getPosition();
        BlockState blockState = world.getBlockState(position);

        if (blockState.isOf(Blocks.CAULDRON)) {
            int level = blockState.get(CauldronBlock.LEVEL);
            int extracted = Math.toIntExact(Math.min(level, maxQuantity / FluidConstants.BOTTLE)); // should be within int range
            int resultLevel = level - extracted;

            setLevel(context, world, position, blockState, resultLevel);

            return extracted * FluidConstants.BOTTLE;
        }

        return 0L;
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        WorldAccess world = getWorld();
        BlockPos position = getPosition();
        BlockState blockState = world.getBlockState(position);

        if (blockState.isOf(Blocks.CAULDRON)) {
            int level = blockState.get(CauldronBlock.LEVEL);
            int inserted = Math.toIntExact(Math.min(maxQuantity / FluidConstants.BOTTLE, 3 - level)); // should be within int range
            int resultLevel = level + inserted;

            setLevel(context, world, position, blockState, resultLevel);

            return maxQuantity - inserted * FluidConstants.BOTTLE;
        }

        return maxQuantity;
    }

    @Override
    protected long insertNew(Context context, Content content, Fluid type, long maxQuantity) {
        if (type != Fluids.WATER)
            return maxQuantity;
        return insertCurrent(context, maxQuantity);
    }

    protected static void setLevel(Context context, WorldAccess world, BlockPos position, BlockState state, int level) {
        BlockState resultState = state.with(CauldronBlock.LEVEL, level);
        context.configure(
                () -> world.setBlockState(position, resultState, WorldUtilities.NO_REDRAW | WorldUtilities.SKIP_DROPS),
                () -> world.setBlockState(position, state, WorldUtilities.NO_REDRAW | WorldUtilities.SKIP_DROPS)
        );
        context.execute(() -> {
            world.setBlockState(position, state, WorldUtilities.NO_REDRAW | WorldUtilities.SKIP_DROPS);
            world.setBlockState(position, resultState, WorldUtilities.DEFAULT_FLAGS);
        });
    }

    protected WorldAccess getWorld() {
        return world;
    }

    protected BlockPos getPosition() {
        return position;
    }

    @Override
    public Content getContent() {
        return FluidContent.of(Fluids.WATER); // locked to water
    }

    @Override
    public long getQuantity() {
        BlockState blockState = getWorld().getBlockState(getPosition());
        if (blockState.isOf(Blocks.CAULDRON))
            return FluidConstants.BOTTLE * blockState.get(CauldronBlock.LEVEL);
        return 0L;
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(FluidConstants.BUCKET);
    }

    @Override
    protected Collection<? extends Class<? extends PushEvent>> getSupportedPushEvents() {
        // capacity is fixed, effectively support
        return SUPPORTED_PUSH_EVENTS; // world set block state
    }

    @Override
    protected Collection<? extends Class<? extends PullEvent>> getSupportedPullEvents() {
        return SUPPORTED_PULL_EVENTS; // world set block state
    }

    @Override
    public Object getRevisionFor(Class<? extends PullEvent> event) {
        if (event == TransferNetEvent.class)
            return getQuantity();
        return super.getRevisionFor(event);
    }

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> {
            if (query instanceof ContentQuery
                    && !((ContentQuery) query).getContent().equals(FluidContent.of(Fluids.WATER)))
                return TriState.FALSE;
            if (query instanceof TransferQuery)
                return TriState.TRUE;
            if (query instanceof StoreQuery)
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
    }
}
