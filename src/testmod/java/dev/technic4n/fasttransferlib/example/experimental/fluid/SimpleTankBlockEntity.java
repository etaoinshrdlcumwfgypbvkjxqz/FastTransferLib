package dev.technic4n.fasttransferlib.example.experimental.fluid;

import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.experimental.api.view.ViewApi;
import dev.technici4n.fasttransferlib.experimental.impl.base.MonoStorageAtom;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;

public class SimpleTankBlockEntity extends BlockEntity {
	private final MonoStorageAtom<Fluid> storage = new MonoStorageAtom<>(Fluid.class, FluidConstants.BUCKET * 10);

	public SimpleTankBlockEntity() {
		super(FluidExample.SIMPLE_TANK_BLOCK_ENTITY);
	}

	public static void init() {
		TransferApi.BLOCK.registerForBlockEntities((blockentity, context) -> ((SimpleTankBlockEntity) blockentity).getStorage(),
				FluidExample.SIMPLE_TANK_BLOCK_ENTITY);
		ViewApi.BLOCK.registerForBlockEntities((blockentity, context) -> ((SimpleTankBlockEntity) blockentity).getStorage(),
				FluidExample.SIMPLE_TANK_BLOCK_ENTITY);
	}

	protected MonoStorageAtom<Fluid> getStorage() {
		return storage;
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		tag.put("storage", getStorage().toTag());
		return tag;
	}

	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		getStorage().fromTag(tag.getCompound("storage"));
	}
}
