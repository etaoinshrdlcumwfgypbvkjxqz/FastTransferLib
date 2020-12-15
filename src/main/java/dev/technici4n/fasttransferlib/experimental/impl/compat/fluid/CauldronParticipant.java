package dev.technici4n.fasttransferlib.experimental.impl.compat.fluid;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.participant.SingleCategoryParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CauldronParticipant
        extends SingleCategoryParticipant<Fluid> {
    private final World world;
    private final BlockPos position;

    public CauldronParticipant(World world, BlockPos position) {
        super(Fluid.class);
        this.world = world;
        this.position = position.toImmutable();
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        if (type != Fluids.WATER)
            return maxAmount;

        BlockState blockState = world.getBlockState(position);

        if (blockState.isOf(Blocks.CAULDRON)) {
            int level = blockState.get(CauldronBlock.LEVEL);
            int inserted = Math.toIntExact(Math.min(maxAmount / 27000, 3 - level)); // should be within int range
            int resultLevel = level + inserted;

            context.execute(
                    () -> world.setBlockState(position, blockState.with(CauldronBlock.LEVEL, resultLevel)),
                    () -> world.setBlockState(position, blockState)
            );

            return maxAmount - inserted * 27000L;
        }

        return maxAmount;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        if (type != Fluids.WATER)
            return 0L;

        BlockState blockState = world.getBlockState(position);

        if (blockState.isOf(Blocks.CAULDRON)) {
            int level = blockState.get(CauldronBlock.LEVEL);
            int extracted = Math.toIntExact(Math.min(level, maxAmount / 27000)); // should be within int range
            int resultLevel = level - extracted;

            context.execute(
                    () -> world.setBlockState(position, blockState.with(CauldronBlock.LEVEL, resultLevel)),
                    () -> world.setBlockState(position, blockState)
            );

            return extracted * 27000L;
        }

        return 0L;
    }
}
