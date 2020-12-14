package dev.technici4n.fasttransferlib.experimental.transfer.impl.compat;

import dev.technici4n.fasttransferlib.experimental.transfer.api.Participant;
import dev.technici4n.fasttransferlib.experimental.transfer.api.TransferApi;
import dev.technici4n.fasttransferlib.experimental.transfer.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.compat.fluid.BottleParticipant;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.compat.fluid.BucketParticipant;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.compat.fluid.CauldronParticipant;
import dev.technici4n.fasttransferlib.experimental.transfer.impl.compat.item.InventorySidedView;
import net.fabricmc.fabric.api.provider.v1.block.BlockApiLookup;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum VanillaCompat {
    ;

    public static void initializeClass() {
        ItemCompat.initializeClass();
        FluidCompat.initializeClass();
    }

    public enum ItemCompat {
        ;

        static {
            BlockApiLookup.BlockEntityApiProvider<Participant, BlockLookupContext> inventoryProvider = (blockEntity, context) -> {
                if (blockEntity instanceof Inventory) {
                    return new InventorySidedView((Inventory) blockEntity, context.getDirection());
                } else {
                    return null;
                }
            };

            // Vanilla containers, for optimal performance
            TransferApi.BLOCK.registerForBlockEntities(inventoryProvider,
                    BlockEntityType.DISPENSER, BlockEntityType.DROPPER, BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE,
                    BlockEntityType.SMOKER, BlockEntityType.BARREL, BlockEntityType.BREWING_STAND, BlockEntityType.HOPPER,
                    BlockEntityType.SHULKER_BOX);
            TransferApi.BLOCK.registerForBlocks((world, pos, state, context) -> {
                Inventory inv = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true);
                return inv == null ? null : new InventorySidedView(inv, context.getDirection());
            }, Blocks.CHEST, Blocks.TRAPPED_CHEST);

            // Fallback for vanilla interfaces
            TransferApi.BLOCK.registerBlockEntityFallback(inventoryProvider);
            TransferApi.BLOCK.registerBlockFallback((world, pos, state, context) -> {
                if (state.getBlock() instanceof InventoryProvider) {
                    Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);

                    if (inv != null) return new InventorySidedView(inv, context.getDirection());
                }

                return null;
            });
        }

        public static void initializeClass() {}
    }

    public enum FluidCompat {
        ;

        static {
            TransferApi.BLOCK.registerForBlocks((world, pos, state, direction) -> new CauldronParticipant(world, pos),
                    Blocks.CAULDRON);
            TransferApi.ITEM.register(BottleParticipant::new, Items.POTION, Items.GLASS_BOTTLE);
            TransferApi.ITEM.registerFallback((item, context) -> {
                Item item1 = item.asItem();
                if (!(item1 instanceof BucketItem)) return null;
                if (item1 instanceof FishBucketItem) return null;
                return new BucketParticipant(item1, context);
            });
        }

        public static void initializeClass() {}
    }
}
