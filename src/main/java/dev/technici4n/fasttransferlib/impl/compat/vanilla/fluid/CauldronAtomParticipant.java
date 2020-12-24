package dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.impl.util.WorldUtilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class CauldronAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid>
        implements Atom {
    private final WorldAccess world;
    private final BlockPos position;

    public CauldronAtomParticipant(WorldAccess world, BlockPos position) {
        super(Fluid.class);
        this.world = world;
        this.position = position.toImmutable();
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        if (type != Fluids.WATER)
            return maxAmount;

        WorldAccess world = getWorld();
        BlockPos position = getPosition();
        BlockState blockState = world.getBlockState(position);

        if (blockState.isOf(Blocks.CAULDRON)) {
            int level = blockState.get(CauldronBlock.LEVEL);
            int inserted = Math.toIntExact(Math.min(maxAmount / FluidConstants.BOTTLE, 3 - level)); // should be within int range
            int resultLevel = level + inserted;

            setLevel(context, world, position, blockState, resultLevel);

            return maxAmount - inserted * FluidConstants.BOTTLE;
        }

        return maxAmount;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        if (type != Fluids.WATER)
            return 0L;

        WorldAccess world = getWorld();
        BlockPos position = getPosition();
        BlockState blockState = world.getBlockState(position);

        if (blockState.isOf(Blocks.CAULDRON)) {
            int level = blockState.get(CauldronBlock.LEVEL);
            int extracted = Math.toIntExact(Math.min(level, maxAmount / FluidConstants.BOTTLE)); // should be within int range
            int resultLevel = level - extracted;

            setLevel(context, world, position, blockState, resultLevel);

            return extracted * FluidConstants.BOTTLE;
        }

        return 0L;
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
        return getAmount() == 0L ? EmptyContent.INSTANCE : FluidContent.of(Fluids.WATER);
    }

    @Override
    public long getAmount() {
        BlockState blockState = getWorld().getBlockState(getPosition());
        if (blockState.isOf(Blocks.CAULDRON))
            return FluidConstants.BOTTLE * blockState.get(CauldronBlock.LEVEL);
        return 0L;
    }
}
