package dev.technici4n.fasttransferlib.impl.compat.vanilla;

import dev.technici4n.fasttransferlib.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.api.view.ViewApi;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid.BottleAtomParticipant;
import dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid.BucketAtomParticipant;
import dev.technici4n.fasttransferlib.impl.compat.vanilla.fluid.CauldronAtomParticipant;
import dev.technici4n.fasttransferlib.impl.compat.vanilla.item.SidedInventoryViewParticipant;
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
import org.jetbrains.annotations.Nullable;

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
            TransferApi.BLOCK.registerForBlockEntities(ItemCompat::getBlockEntityViewParticipant,
                    BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE,
                    BlockEntityType.SMOKER, BlockEntityType.BARREL, BlockEntityType.BREWING_STAND, BlockEntityType.HOPPER,
                    BlockEntityType.SHULKER_BOX);
            TransferApi.BLOCK.registerForBlocks(ItemCompat::getChestViewParticipant, Blocks.CHEST, Blocks.TRAPPED_CHEST);

            ViewApi.BLOCK.registerForBlockEntities(ItemCompat::getBlockEntityViewParticipant,
                    BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE,
                    BlockEntityType.SMOKER, BlockEntityType.BARREL, BlockEntityType.BREWING_STAND, BlockEntityType.HOPPER,
                    BlockEntityType.SHULKER_BOX);
            ViewApi.BLOCK.registerForBlocks(ItemCompat::getChestViewParticipant, Blocks.CHEST, Blocks.TRAPPED_CHEST);

            // Fallback for vanilla interfaces
            TransferApi.BLOCK.registerFallback(ItemCompat::getBlockFallbackViewParticipant);
            ViewApi.BLOCK.registerFallback(ItemCompat::getBlockFallbackViewParticipant);
        }

        public static void initializeClass() {}

        private static AbstractMonoCategoryViewParticipant<Item> getBlockEntityViewParticipant(BlockEntity entity, BlockLookupContext context) {
            if (entity instanceof Inventory)
                return SidedInventoryViewParticipant.of((Inventory) entity, context.getDirection());
            return null;
        }

        private static AbstractMonoCategoryViewParticipant<Item> getBlockFallbackViewParticipant(World world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, BlockLookupContext context) {
            if (entity != null) {
                AbstractMonoCategoryViewParticipant<Item> result = getBlockEntityViewParticipant(entity, context);
                if (result != null)
                    return result;
            }
            if (state.getBlock() instanceof InventoryProvider) {
                Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);
                if (inv != null) return SidedInventoryViewParticipant.of(inv, context.getDirection());
            }
            return null;
        }

        private static AbstractMonoCategoryViewParticipant<Item> getChestViewParticipant(World world, BlockPos pos, BlockState state, BlockLookupContext context) {
            Inventory inv = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true);
            return inv == null ? null : SidedInventoryViewParticipant.of(inv, context.getDirection());
        }
    }

    public enum FluidCompat {
        ;

        static {
            TransferApi.BLOCK.registerForBlocks(FluidCompat::getCauldronViewParticipant,
                    Blocks.CAULDRON);
            TransferApi.ITEM.register(FluidCompat::getBottleAtomParticipant, Items.POTION, Items.GLASS_BOTTLE);
            TransferApi.ITEM.registerFallback(FluidCompat::getBucketFallbackViewParticipant);

            ViewApi.BLOCK.registerForBlocks(FluidCompat::getCauldronViewParticipant,
                    Blocks.CAULDRON);
            ViewApi.ITEM.register(FluidCompat::getBottleAtomParticipant, Items.POTION, Items.GLASS_BOTTLE);
            ViewApi.ITEM.registerFallback(FluidCompat::getBucketFallbackViewParticipant);
        }

        public static void initializeClass() {}

        private static BucketAtomParticipant getBucketFallbackViewParticipant(ItemConvertible item, ItemLookupContext context) {
            Item item1 = item.asItem();
            if (item1 instanceof BucketItem && !(item1 instanceof FishBucketItem))
                return new BucketAtomParticipant(item1, context);
            return null;
        }

        private static CauldronAtomParticipant getCauldronViewParticipant(World world, BlockPos pos, BlockState state, BlockLookupContext direction) {
            return new CauldronAtomParticipant(world, pos);
        }

        private static BottleAtomParticipant getBottleAtomParticipant(ItemConvertible item, ItemLookupContext lookupContext) {
            return new BottleAtomParticipant(lookupContext);
        }
    }
}
