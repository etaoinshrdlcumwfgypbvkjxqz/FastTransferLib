package dev.technici4n.fasttransferlib.impl.compat.lba;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.item.ItemTransferable;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import net.minecraft.item.ItemStack;

public enum LbaCompatImplUtil {
    ;

    public static long genericInsertImpl(Object object, Context context, Content itemContent, long maxAmount) {
        if (object instanceof ItemTransferable)
            return insertImpl((ItemTransferable) object, context, itemContent, maxAmount);
        else if (object instanceof FluidTransferable)
            return insertImpl((FluidTransferable) object, context, itemContent, maxAmount);
        return maxAmount;
    }

    public static long genericExtractImpl(Object object, Context context, Content itemContent, long maxAmount) {
        if (object instanceof ItemTransferable)
            return extractImpl((ItemTransferable) object, context, itemContent, maxAmount);
        else if (object instanceof FluidTransferable)
            return extractImpl((FluidTransferable) object, context, itemContent, maxAmount);
        return 0L;
    }

    public static long insertImpl(ItemTransferable transferable, Context context, Content itemContent, long maxAmount) {
        /* note
        This assumes that an insertion can always be reverted by an extraction that follows the insertion,
        which may be reasonable for most cases.
        However, if this is violated, things may go wrong.
         */
        int amount1 = Ints.saturatedCast(maxAmount);

        ItemStack leftover = transferable.attemptInsertion(ItemContent.asStack(itemContent, amount1), Simulation.SIMULATE);
        int leftoverAmount = leftover.getCount();

        if (amount1 != leftoverAmount) {
            int insert = amount1 - leftoverAmount;
            context.configure(() -> transferable.insert(ItemContent.asStack(itemContent, insert)),
                    () -> transferable.extract(ItemContent.asStack(itemContent, insert), insert));
            return maxAmount - insert;
        }
        return maxAmount;
    }

    public static long extractImpl(ItemTransferable transferable, Context context, Content itemContent, long maxAmount) {
        /* note
        This assumes that an extraction can always be reverted by an insertion that follows the extraction,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        int amount1 = Ints.saturatedCast(maxAmount);

        ItemStack extracted = transferable.attemptExtraction(new ExactItemStackFilter(ItemContent.asStack(itemContent, amount1)), amount1, Simulation.SIMULATE);
        int extractedAmount = extracted.getCount();

        if (extractedAmount > 0) {
            context.configure(() -> transferable.extract(new ExactItemStackFilter(ItemContent.asStack(itemContent, amount1)), extractedAmount),
                    () -> transferable.insert(ItemContent.asStack(itemContent, amount1)));
            return extractedAmount;
        }
        return 0L;
    }


    public static long insertImpl(FluidTransferable transferable, Context context, Content fluidContent, long maxAmount) {
        /* note
        This assumes that an insertion can always be reverted by an extraction that follows the insertion,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        FluidKey key = LbaCompatUtil.asFluidKey(fluidContent);
        assert key != null;
        FluidAmount tryAmount = LbaCompatUtil.asFluidAmount(maxAmount);

        FluidVolume leftover = transferable.attemptInsertion(key.withAmount(tryAmount), Simulation.SIMULATE);
        FluidAmount leftoverAmount = leftover.getAmount_F();

        if (!tryAmount.equals(leftoverAmount)) {
            FluidAmount insert = tryAmount.sub(leftoverAmount);
            context.configure(() -> transferable.insert(key.withAmount(insert)),
                    () -> transferable.extract(key, insert));
            return LbaCompatUtil.asAmount(insert);
        }
        return maxAmount;
    }

    public static long extractImpl(FluidTransferable transferable, Context context, Content fluidContent, long maxAmount) {
        /* note
        This assumes that an extraction can always be reverted by an insertion that follows the extraction,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        FluidKey key = LbaCompatUtil.asFluidKey(fluidContent);
        assert key != null;
        FluidAmount tryAmount = LbaCompatUtil.asFluidAmount(maxAmount);

        FluidVolume extracted = transferable.attemptExtraction(ExactFluidFilter.of(key), tryAmount, Simulation.SIMULATE);
        FluidAmount extractedAmount = extracted.getAmount_F();

        if (extractedAmount.isPositive()) {
            context.configure(() -> transferable.extract(key, extractedAmount),
                    () -> transferable.insert(key.withAmount(extractedAmount)));
            return LbaCompatUtil.asAmount(extractedAmount);
        }
        return 0L;
    }
}
