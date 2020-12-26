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

    public static long genericInsertImpl(Object object, Context context, Content itemContent, long maxQuantity) {
        if (object instanceof ItemTransferable)
            return insertImpl((ItemTransferable) object, context, itemContent, maxQuantity);
        else if (object instanceof FluidTransferable)
            return insertImpl((FluidTransferable) object, context, itemContent, maxQuantity);
        return maxQuantity;
    }

    public static long genericExtractImpl(Object object, Context context, Content itemContent, long maxQuantity) {
        if (object instanceof ItemTransferable)
            return extractImpl((ItemTransferable) object, context, itemContent, maxQuantity);
        else if (object instanceof FluidTransferable)
            return extractImpl((FluidTransferable) object, context, itemContent, maxQuantity);
        return 0L;
    }

    public static long insertImpl(ItemTransferable transferable, Context context, Content itemContent, long maxQuantity) {
        /* note
        This assumes that an insertion can always be reverted by an extraction that follows the insertion,
        which may be reasonable for most cases.
        However, if this is violated, things may go wrong.
         */
        int quantity1 = Ints.saturatedCast(maxQuantity);

        ItemStack leftover = transferable.attemptInsertion(ItemContent.asStack(itemContent, quantity1), Simulation.SIMULATE);
        int leftoverQuantity = leftover.getCount();

        if (quantity1 != leftoverQuantity) {
            int insert = quantity1 - leftoverQuantity;
            context.configure(() -> transferable.insert(ItemContent.asStack(itemContent, insert)),
                    () -> transferable.extract(ItemContent.asStack(itemContent, insert), insert));
            return maxQuantity - insert;
        }
        return maxQuantity;
    }

    public static long extractImpl(ItemTransferable transferable, Context context, Content itemContent, long maxQuantity) {
        /* note
        This assumes that an extraction can always be reverted by an insertion that follows the extraction,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        int quantity1 = Ints.saturatedCast(maxQuantity);

        ItemStack extracted = transferable.attemptExtraction(new ExactItemStackFilter(ItemContent.asStack(itemContent, quantity1)), quantity1, Simulation.SIMULATE);
        int extractedQuantity = extracted.getCount();

        if (extractedQuantity > 0) {
            context.configure(() -> transferable.extract(new ExactItemStackFilter(ItemContent.asStack(itemContent, quantity1)), extractedQuantity),
                    () -> transferable.insert(ItemContent.asStack(itemContent, quantity1)));
            return extractedQuantity;
        }
        return 0L;
    }

    public static long insertImpl(FluidTransferable transferable, Context context, Content fluidContent, long maxQuantity) {
        /* note
        This assumes that an insertion can always be reverted by an extraction that follows the insertion,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        FluidKey key = asFluidKey(fluidContent);
        assert key != null;
        FluidAmount tryQuantity = asFluidAmount(maxQuantity);

        FluidVolume leftover = transferable.attemptInsertion(key.withAmount(tryQuantity), Simulation.SIMULATE);
        FluidAmount leftoverQuantity = leftover.getAmount_F();

        if (!tryQuantity.equals(leftoverQuantity)) {
            FluidAmount insert = tryQuantity.sub(leftoverQuantity);
            context.configure(() -> transferable.insert(key.withAmount(insert)),
                    () -> transferable.extract(key, insert));
            return asQuantity(insert);
        }
        return maxQuantity;
    }

    public static long extractImpl(FluidTransferable transferable, Context context, Content fluidContent, long maxQuantity) {
        /* note
        This assumes that an extraction can always be reverted by an insertion that follows the extraction,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        FluidKey key = asFluidKey(fluidContent);
        assert key != null;
        FluidAmount tryQuantity = asFluidAmount(maxQuantity);

        FluidVolume extracted = transferable.attemptExtraction(ExactFluidFilter.of(key), tryQuantity, Simulation.SIMULATE);
        FluidAmount extractedQuantity = extracted.getAmount_F();

        if (extractedQuantity.isPositive()) {
            context.configure(() -> transferable.extract(key, extractedQuantity),
                    () -> transferable.insert(key.withAmount(extractedQuantity)));
            return asQuantity(extractedQuantity);
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

    public static long asQuantity(FluidVolume fluidVolume) {
        return asQuantity(fluidVolume.getAmount_F());
    }

    public static long asQuantity(FluidAmount fluidAmount) {
        return fluidAmount.asLong(FluidConstants.BUCKET, RoundingMode.DOWN);
    }

    public static BigInteger asBigQuantity(FluidVolume fluidVolume) {
        return asBigQuantity(fluidVolume.getAmount_F());
    }

    public static BigInteger asBigQuantity(FluidAmount fluidAmount) {
        return fluidAmount.bigMul(BUCKET).whole;
    }

    public static FluidAmount asFluidAmount(long quantity) {
        return FluidAmount.of(quantity, FluidConstants.BUCKET);
    }

    public static FluidVolume asFluidVolume(Content fluidContent, long quantity) {
        return asFluidKey(fluidContent).withAmount(asFluidAmount(quantity));
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
