package dev.technici4n.fasttransferlib.impl.compat.lba;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.context.StatelessContext;
import dev.technici4n.fasttransferlib.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.impl.context.ExecutionContext;
import dev.technici4n.fasttransferlib.impl.context.SimulationContext;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.fluid.Fluid;

import java.math.RoundingMode;

public enum LbaCompatUtil {
    ;

    public static FluidAmount abs(FluidAmount fluidAmount) {
        if (fluidAmount.isNegative())
            return fluidAmount.negate();
        return fluidAmount;
    }

    public static class FluidAmountAsAmountsIterator
            implements LongIterator {
        private FluidAmount fluidAmount;

        protected FluidAmountAsAmountsIterator(FluidAmount fluidAmount) {
            this.fluidAmount = fluidAmount;
        }

        public static FluidAmountAsAmountsIterator of(FluidAmount fluidAmount) {
            return new FluidAmountAsAmountsIterator(fluidAmount);
        }

        public static FluidAmountAsAmountsIterator of(FluidVolume fluidVolume) {
            return new FluidAmountAsAmountsIterator(fluidVolume.getAmount_F());
        }

        @Override
        public long nextLong() {
            if (hasNext()) {
                FluidAmount fluidAmount = getFluidAmount();
                long next = asAmount(fluidAmount);
                setFluidAmount(fluidAmount.sub(asFluidAmount(next)));
                return next;
            }
            return 0L;
        }

        @Override
        public boolean hasNext() {
            return !getFluidAmount().isZero();
        }

        protected FluidAmount getFluidAmount() {
            return fluidAmount;
        }

        protected void setFluidAmount(FluidAmount fluidAmount) {
            this.fluidAmount = fluidAmount;
        }
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
}
