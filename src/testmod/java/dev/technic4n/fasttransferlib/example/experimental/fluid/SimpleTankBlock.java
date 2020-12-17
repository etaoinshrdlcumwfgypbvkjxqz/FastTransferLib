package dev.technic4n.fasttransferlib.example.experimental.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidTextHelper;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;
import dev.technici4n.fasttransferlib.experimental.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.ViewApi;
import dev.technici4n.fasttransferlib.experimental.impl.lookup.BlockLookupContextImpl;
import dev.technici4n.fasttransferlib.experimental.impl.lookup.PlayerItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.context.ExecutionContext;
import dev.technici4n.fasttransferlib.experimental.impl.view.FluidUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SimpleTankBlock extends Block implements BlockEntityProvider {
	public SimpleTankBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new SimpleTankBlockEntity();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		Participant participant = TransferApi.BLOCK.get(world, pos, BlockLookupContextImpl.of(hit.getSide()));
		View view = ViewApi.BLOCK.get(world, pos, BlockLookupContextImpl.of(hit.getSide()));
		View itemView = ViewApi.ITEM.get(player.getStackInHand(hand).getItem(), PlayerItemLookupContext.ofHand(player, hand));
		if (participant != null && view != null && itemView != null) {
			if (!world.isClient()) {
				FluidUtilities.moveAll(ExecutionContext.getInstance(), itemView, participant, content -> content.getCategory() == Fluid.class);
				view.getAmounts().forEach((content, amount) -> {
					player.sendMessage(
							new LiteralText(String.format("Tank Now At %s millibuckets of %s",
									FluidTextHelper.getUnicodeMillibuckets(amount, true), content)),
							false);
					player.sendMessage(
							new LiteralText(String.format("Tank Now At %s millibuckets of %s",
									FluidTextHelper.getUnicodeMillibuckets(amount, false), content)),
							false);
				});
			}
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
