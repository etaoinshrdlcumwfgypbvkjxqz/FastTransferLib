package dev.technic4n.fasttransferlib.example.experimental.fluid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidExample implements ModInitializer {
	public static final String MOD_ID = "fasttransferlib-testmod-exp";
	public final SimpleTankBlock SIMPLE_TANK_BLOCK = new SimpleTankBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
	public static BlockEntityType<SimpleTankBlockEntity> SIMPLE_TANK_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "yeet_stick"), new YeetStick(new Item.Settings()));
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "simple_tank_block"),
				SIMPLE_TANK_BLOCK);
		SIMPLE_TANK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "simple_tank_block"),
				BlockEntityType.Builder.create(SimpleTankBlockEntity::new, SIMPLE_TANK_BLOCK).build(null));
		SimpleTankBlockEntity.init();
	}
}
