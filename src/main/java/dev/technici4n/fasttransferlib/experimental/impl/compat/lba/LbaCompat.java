package dev.technici4n.fasttransferlib.experimental.impl.compat.lba;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import dev.technici4n.fasttransferlib.experimental.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.ViewApi;
import dev.technici4n.fasttransferlib.experimental.impl.compat.lba.fluid.LbaFluidToViewParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.lba.fluid.LbaFluidTransferableFromView;
import dev.technici4n.fasttransferlib.experimental.impl.compat.lba.item.LbaItemToViewParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.lba.item.LbaItemTransferableFromView;
import dev.technici4n.fasttransferlib.experimental.impl.lookup.BlockLookupContextImpl;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public enum LbaCompat {
    ;

    public enum ItemCompat {
        ;

        @SuppressWarnings("unused")
        public static void initializeClass() {}

        private static boolean inCompat = false;

        static {
            registerLbaInFtl();
            registerFtlInLba();
        }

        private static void registerLbaInFtl() {
            TransferApi.BLOCK.registerFallback(ItemCompat::getBlockFallbackViewParticipant);
            ViewApi.BLOCK.registerFallback(ItemCompat::getBlockFallbackViewParticipant);
        }

        private static void registerFtlInLba() {
            ItemAttributes.EXTRACTABLE.appendBlockAdder(ItemCompat::getBlockAdder);
            ItemAttributes.INSERTABLE.appendBlockAdder(ItemCompat::getBlockAdder);
            // ItemAttributes.FIXED_INV_VIEW.appendBlockAdder(ItemCompat::getBlockAdder);
        }

        private static void getBlockAdder(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
            Direction dir = to.getTargetSide();

            if (dir != null) {
                if (!inCompat) {
                    inCompat = true;
                    @Nullable View view = ViewApi.BLOCK.get(world, pos, BlockLookupContextImpl.of(dir));
                    if (view != null)
                        to.offer(LbaItemTransferableFromView.of(view));
                    inCompat = false;
                }
            }
        }

        private static LbaItemToViewParticipant getBlockFallbackViewParticipant(World world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, BlockLookupContext context) {
            if (inCompat) return null;

            Direction direction = context.getDirection();

            inCompat = true;
            AttributeList<FixedItemInv> to = ItemAttributes.FIXED_INV.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
            inCompat = false;

            return to.hasOfferedAny()
                    ? LbaItemToViewParticipant.of(to.combine(ItemAttributes.FIXED_INV))
                    : null;
        }
    }

    public enum FluidCompat {
        ;

        @SuppressWarnings("unused")
        public static void initializeClass() {}

        private static boolean inCompat = false;

        static {
            registerLbaInFtl();
            registerFtlInLba();
        }

        private static void registerLbaInFtl() {
            TransferApi.BLOCK.registerFallback(FluidCompat::getBlockFallbackViewParticipant);
            ViewApi.BLOCK.registerFallback(FluidCompat::getBlockFallbackViewParticipant);
        }

        private static void registerFtlInLba() {
            FluidAttributes.EXTRACTABLE.appendBlockAdder(FluidCompat::getBlockAdder);
            FluidAttributes.INSERTABLE.appendBlockAdder(FluidCompat::getBlockAdder);
            // FluidAttributes.FIXED_INV_VIEW.appendBlockAdder(ItemCompat::getBlockAdder);
        }

        private static void getBlockAdder(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
            Direction dir = to.getTargetSide();

            if (dir != null) {
                if (!inCompat) {
                    inCompat = true;
                    @Nullable View view = ViewApi.BLOCK.get(world, pos, BlockLookupContextImpl.of(dir));
                    if (view != null)
                        to.offer(LbaFluidTransferableFromView.of(view));
                    inCompat = false;
                }
            }
        }

        private static LbaFluidToViewParticipant getBlockFallbackViewParticipant(World world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, BlockLookupContext context) {
            if (inCompat) return null;

            Direction direction = context.getDirection();

            inCompat = true;
            AttributeList<FixedFluidInv> to = FluidAttributes.FIXED_INV.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
            inCompat = false;

            if (to.hasOfferedAny()) {
                return LbaFluidToViewParticipant.of(to.combine(FluidAttributes.FIXED_INV));
            } else {
                return null;
            }
        }
    }
}
