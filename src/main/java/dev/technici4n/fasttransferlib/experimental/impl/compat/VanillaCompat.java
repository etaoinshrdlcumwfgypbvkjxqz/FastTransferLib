package dev.technici4n.fasttransferlib.experimental.impl.compat;

import dev.technici4n.fasttransferlib.experimental.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;
import dev.technici4n.fasttransferlib.experimental.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.ViewApi;
import dev.technici4n.fasttransferlib.experimental.impl.compat.fluid.BottleAtomParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.fluid.BucketAtomParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.fluid.CauldronAtomParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.item.SidedInventoryViewParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum VanillaCompat {
    ;

    public static void initializeClass() {
        ItemCompat.initializeClass();
        FluidCompat.initializeClass();
    }

    public enum ItemCompat {
        ;

        static {
            // Vanilla containers, for optimal performance
            TransferApi.BLOCK.registerForBlockEntities(ItemCompat::getBlockEntityFallbackParticipant,
                    BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE,
                    BlockEntityType.SMOKER, BlockEntityType.BARREL, BlockEntityType.BREWING_STAND, BlockEntityType.HOPPER,
                    BlockEntityType.SHULKER_BOX);
            TransferApi.BLOCK.registerForBlocks(ItemCompat::getChestParticipant, Blocks.CHEST, Blocks.TRAPPED_CHEST);

            ViewApi.BLOCK.registerForBlockEntities(ItemCompat::getBlockEntityFallbackView,
                    BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE,
                    BlockEntityType.SMOKER, BlockEntityType.BARREL, BlockEntityType.BREWING_STAND, BlockEntityType.HOPPER,
                    BlockEntityType.SHULKER_BOX);
            ViewApi.BLOCK.registerForBlocks(ItemCompat::getChestView, Blocks.CHEST, Blocks.TRAPPED_CHEST);

            // Fallback for vanilla interfaces
            TransferApi.BLOCK.registerBlockEntityFallback(ItemCompat::getBlockEntityFallbackParticipant);
            TransferApi.BLOCK.registerBlockFallback(ItemCompat::getBlockFallbackParticipant);

            ViewApi.BLOCK.registerBlockEntityFallback(ItemCompat::getBlockEntityFallbackView);
            ViewApi.BLOCK.registerBlockFallback(ItemCompat::getBlockFallbackView);
        }

        public static void initializeClass() {}

        private static Participant getBlockEntityFallbackParticipant(BlockEntity blockEntity, BlockLookupContext context) {
            return blockEntity instanceof Inventory
                    ? SidedInventoryViewParticipant.ofParticipant((Inventory) blockEntity, context.getDirection())
                    : null;
        }

        private static Participant getBlockFallbackParticipant(World world, BlockPos pos, BlockState state, BlockLookupContext context) {
            if (state.getBlock() instanceof InventoryProvider) {
                Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);
                if (inv != null) return SidedInventoryViewParticipant.ofParticipant(inv, context.getDirection());
            }
            return null;
        }

        private static Participant getChestParticipant(World world, BlockPos pos, BlockState state, BlockLookupContext context) {
            Inventory inv = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true);
            return inv == null ? null : SidedInventoryViewParticipant.ofParticipant(inv, context.getDirection());
        }

        private static View getBlockEntityFallbackView(BlockEntity blockEntity, BlockLookupContext context) {
            return blockEntity instanceof Inventory
                    ? SidedInventoryViewParticipant.ofView((Inventory) blockEntity, context.getDirection())
                    : null;
        }

        private static View getBlockFallbackView(World world, BlockPos pos, BlockState state, BlockLookupContext context) {
            if (state.getBlock() instanceof InventoryProvider) {
                Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);
                if (inv != null) return SidedInventoryViewParticipant.ofView(inv, context.getDirection());
            }
            return null;
        }

        private static View getChestView(World world, BlockPos pos, BlockState state, BlockLookupContext context) {
            Inventory inv = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true);
            return inv == null ? null : SidedInventoryViewParticipant.ofView(inv, context.getDirection());
        }
    }

    public enum FluidCompat {
        ;

        static {
            TransferApi.BLOCK.registerForBlocks(FluidCompat::getCauldronViewParticipant,
                    Blocks.CAULDRON);
            TransferApi.ITEM.register(BottleAtomParticipant::new, Items.POTION, Items.GLASS_BOTTLE);
            TransferApi.ITEM.registerFallback(FluidCompat::getBucketFallbackViewParticipant);

            ViewApi.BLOCK.registerForBlocks(FluidCompat::getCauldronViewParticipant,
                    Blocks.CAULDRON);
            ViewApi.ITEM.register(BottleAtomParticipant::new, Items.POTION, Items.GLASS_BOTTLE);
            ViewApi.ITEM.registerFallback(FluidCompat::getBucketFallbackViewParticipant);
        }

        public static void initializeClass() {}

        private static BucketAtomParticipant getBucketFallbackViewParticipant(ItemConvertible item, ItemLookupContext context) {
            Item item1 = item.asItem();
            if (!(item1 instanceof BucketItem)) return null;
            if (item1 instanceof FishBucketItem) return null;
            return new BucketAtomParticipant(item1, context);
        }

        private static CauldronAtomParticipant getCauldronViewParticipant(World world, BlockPos pos, BlockState state, BlockLookupContext direction) {
            return new CauldronAtomParticipant(world, pos);
        }
    }
}
