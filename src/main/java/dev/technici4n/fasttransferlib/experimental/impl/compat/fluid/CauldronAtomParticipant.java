package dev.technici4n.fasttransferlib.experimental.impl.compat.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.experimental.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.FluidContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CauldronAtomParticipant
        extends AbstractMonoCategoryAtom<Fluid>
        implements Atom {
    private final World world;
    private final BlockPos position;

    public CauldronAtomParticipant(World world, BlockPos position) {
        super(Fluid.class);
        this.world = world;
        this.position = position.toImmutable();
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        if (type != Fluids.WATER)
            return maxAmount;

        World world = getWorld();
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

        World world = getWorld();
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

    protected static void setLevel(Context context, World world, BlockPos position, BlockState blockState, int level) {
        context.execute(
                () -> world.setBlockState(position, blockState.with(CauldronBlock.LEVEL, level)),
                () -> world.setBlockState(position, blockState)
        );
    }

    protected World getWorld() {
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
