package dev.technici4n.fasttransferlib.impl.compat.lba;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.item.ItemTransferable;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;
import com.google.common.primitives.Ints;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.context.StatelessContext;
import dev.technici4n.fasttransferlib.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.context.ExecutionContext;
import dev.technici4n.fasttransferlib.impl.context.SimulationContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import java.math.BigInteger;
import java.math.RoundingMode;

public enum LbaCompatUtil {
    ;

    public static FluidAmount abs(FluidAmount fluidAmount) {
        if (fluidAmount.isNegative())
            return fluidAmount.negate();
        return fluidAmount;
    }

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
        FluidKey key = asFluidKey(fluidContent);
        assert key != null;
        FluidAmount tryAmount = asFluidAmount(maxAmount);

        FluidVolume leftover = transferable.attemptInsertion(key.withAmount(tryAmount), Simulation.SIMULATE);
        FluidAmount leftoverAmount = leftover.getAmount_F();

        if (!tryAmount.equals(leftoverAmount)) {
            FluidAmount insert = tryAmount.sub(leftoverAmount);
            context.configure(() -> transferable.insert(key.withAmount(insert)),
                    () -> transferable.extract(key, insert));
            return asAmount(insert);
        }
        return maxAmount;
    }

    public static long extractImpl(FluidTransferable transferable, Context context, Content fluidContent, long maxAmount) {
        /* note
        This assumes that an extraction can always be reverted by an insertion that follows the extraction,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        FluidKey key = asFluidKey(fluidContent);
        assert key != null;
        FluidAmount tryAmount = asFluidAmount(maxAmount);

        FluidVolume extracted = transferable.attemptExtraction(ExactFluidFilter.of(key), tryAmount, Simulation.SIMULATE);
        FluidAmount extractedAmount = extracted.getAmount_F();

        if (extractedAmount.isPositive()) {
            context.configure(() -> transferable.extract(key, extractedAmount),
                    () -> transferable.insert(key.withAmount(extractedAmount)));
            return asAmount(extractedAmount);
        }
        return 0L;
    }

    public static Content asFluidContent(FluidVolume fluidVolume) {
        return asFluidContent(fluidVolume.getFluidKey());
    }

    public static Content asFluidContent(FluidKey fluidKey) {
        return FluidContent.of(fluidKey.getRawFluid());
    }

    public static FluidKey asFluidKey(Content fluidContent) {
        if (fluidContent.isEmpty())
            return FluidKeys.EMPTY;
        assert fluidContent.getCategory() == Fluid.class;
        return FluidKeys.get((Fluid) fluidContent.getType());
    }

    public static long asAmount(FluidVolume fluidVolume) {
        return asAmount(fluidVolume.getAmount_F());
    }

    public static long asAmount(FluidAmount fluidAmount) {
        return fluidAmount.asLong(FluidConstants.BUCKET, RoundingMode.DOWN);
    }

    public static BigInteger asBigAmount(FluidVolume fluidVolume) {
        return asBigAmount(fluidVolume.getAmount_F());
    }

    public static BigInteger asBigAmount(FluidAmount fluidAmount) {
        return fluidAmount.bigMul(BUCKET).whole;
    }

    public static FluidAmount asFluidAmount(long amount) {
        return FluidAmount.of(amount, FluidConstants.BUCKET);
    }

    public static FluidVolume asFluidVolume(Content fluidContent, long amount) {
        return asFluidKey(fluidContent).withAmount(asFluidAmount(amount));
    }

    public static StatelessContext asStatelessContext(Simulation simulation) {
        switch (simulation) {
            case SIMULATE:
                return SimulationContext.getInstance();
            case ACTION:
                return ExecutionContext.getInstance();
            default:
                throw new AssertionError();
        }
    }

    public static final FluidAmount BUCKET = FluidAmount.ofWhole(FluidConstants.BUCKET);
}
