package dev.technici4n.fasttransferlib.impl.compat.lba;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.*;
import alexiil.mc.lib.attributes.item.*;
import dev.technici4n.fasttransferlib.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.ViewApi;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.fluid.LbaFixedFluidToViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.fluid.LbaFluidInvFromView;
import dev.technici4n.fasttransferlib.impl.compat.lba.fluid.LbaGroupedFluidToViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.item.LbaFixedItemToViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.item.LbaGroupedItemToViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.lba.item.LbaItemInvFromView;
import dev.technici4n.fasttransferlib.impl.lookup.BlockLookupContextImpl;
import dev.technici4n.fasttransferlib.impl.util.UncheckedAutoCloseable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
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
            ItemAttributes.FIXED_INV_VIEW.appendBlockAdder(ItemCompat::getBlockAdder);
            ItemAttributes.FIXED_INV.appendBlockAdder(ItemCompat::getBlockAdder);
            ItemAttributes.GROUPED_INV_VIEW.appendBlockAdder(ItemCompat::getBlockAdder);
            ItemAttributes.GROUPED_INV.appendBlockAdder(ItemCompat::getBlockAdder);
            ItemAttributes.INSERTABLE.appendBlockAdder(ItemCompat::getBlockAdder);
            ItemAttributes.EXTRACTABLE.appendBlockAdder(ItemCompat::getBlockAdder);
        }

        private static void getBlockAdder(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
            Direction dir = to.getTargetSide();

            if (dir != null) {
                if (!inCompat) {
                    @Nullable View view;
                    try (UncheckedAutoCloseable ignored = inCompat()) {
                        view = ViewApi.BLOCK.get(world, pos, BlockLookupContextImpl.of(dir));
                    }
                    if (view != null)
                        to.offer(LbaItemInvFromView.of(view));
                }
            }
        }

        private static AbstractMonoCategoryViewParticipant<Item> getBlockFallbackViewParticipant(World world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, BlockLookupContext context) {
            if (inCompat) return null;

            Direction direction = context.getDirection();

            try (UncheckedAutoCloseable ignored = inCompat()) {
                // mutability first, fixed second

                AttributeList<FixedItemInv> toFixed = ItemAttributes.FIXED_INV.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toFixed.hasOfferedAny())
                    return LbaFixedItemToViewParticipant.of(toFixed.combine(ItemAttributes.FIXED_INV));

                AttributeList<GroupedItemInv> toGrouped = ItemAttributes.GROUPED_INV.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toGrouped.hasOfferedAny())
                    return LbaGroupedItemToViewParticipant.of(toGrouped.combine(ItemAttributes.GROUPED_INV));

                AttributeList<FixedItemInvView> toFixedView = ItemAttributes.FIXED_INV_VIEW.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toFixedView.hasOfferedAny())
                    return LbaFixedItemToViewParticipant.of(toFixedView.combine(ItemAttributes.FIXED_INV_VIEW));

                AttributeList<GroupedItemInvView> toGroupedView = ItemAttributes.GROUPED_INV_VIEW.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toGroupedView.hasOfferedAny())
                    return LbaGroupedItemToViewParticipant.of(toGroupedView.combine(ItemAttributes.GROUPED_INV_VIEW));
            }

            return null;
        }

        private static UncheckedAutoCloseable inCompat() {
            assert !inCompat;
            inCompat = true;
            return () -> inCompat = false;
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
            FluidAttributes.FIXED_INV_VIEW.appendBlockAdder(FluidCompat::getBlockAdder);
            FluidAttributes.FIXED_INV.appendBlockAdder(FluidCompat::getBlockAdder);
            FluidAttributes.GROUPED_INV_VIEW.appendBlockAdder(FluidCompat::getBlockAdder);
            FluidAttributes.GROUPED_INV.appendBlockAdder(FluidCompat::getBlockAdder);
            FluidAttributes.INSERTABLE.appendBlockAdder(FluidCompat::getBlockAdder);
            FluidAttributes.EXTRACTABLE.appendBlockAdder(FluidCompat::getBlockAdder);
        }

        private static void getBlockAdder(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
            Direction dir = to.getTargetSide();

            if (dir != null) {
                if (!inCompat) {
                    @Nullable View view;
                    try (UncheckedAutoCloseable ignored = inCompat()) {
                        view = ViewApi.BLOCK.get(world, pos, BlockLookupContextImpl.of(dir));
                    }
                    if (view != null)
                        to.offer(LbaFluidInvFromView.of(view));
                }
            }
        }

        private static AbstractMonoCategoryViewParticipant<Fluid> getBlockFallbackViewParticipant(World world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, BlockLookupContext context) {
            if (inCompat) return null;

            Direction direction = context.getDirection();

            try (UncheckedAutoCloseable ignored = inCompat()) {
                // mutability first, fixed second

                AttributeList<FixedFluidInv> toFixed = FluidAttributes.FIXED_INV.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toFixed.hasOfferedAny())
                    return LbaFixedFluidToViewParticipant.of(toFixed.combine(FluidAttributes.FIXED_INV));

                AttributeList<GroupedFluidInv> toGrouped = FluidAttributes.GROUPED_INV.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toGrouped.hasOfferedAny())
                    return LbaGroupedFluidToViewParticipant.of(toGrouped.combine(FluidAttributes.GROUPED_INV));

                AttributeList<FixedFluidInvView> toFixedView = FluidAttributes.FIXED_INV_VIEW.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toFixedView.hasOfferedAny())
                    return LbaFixedFluidToViewParticipant.of(toFixedView.combine(FluidAttributes.FIXED_INV_VIEW));

                AttributeList<GroupedFluidInvView> toGroupedView = FluidAttributes.GROUPED_INV_VIEW.getAll(world, pos, SearchOptions.inDirection(direction.getOpposite()));
                if (toGroupedView.hasOfferedAny())
                    return LbaGroupedFluidToViewParticipant.of(toGroupedView.combine(FluidAttributes.GROUPED_INV_VIEW));
            }

            return null;
        }

        private static UncheckedAutoCloseable inCompat() {
            assert !inCompat;
            inCompat = true;
            return () -> inCompat = false;
        }
    }
}
