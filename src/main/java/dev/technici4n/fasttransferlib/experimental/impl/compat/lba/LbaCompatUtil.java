package dev.technici4n.fasttransferlib.experimental.impl.compat.lba;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import dev.technici4n.fasttransferlib.experimental.impl.content.FluidContent;
import dev.technici4n.fasttransferlib.experimental.impl.context.ExecutionContext;
import dev.technici4n.fasttransferlib.experimental.impl.context.SimulationContext;
import net.minecraft.fluid.Fluid;

import java.math.RoundingMode;

public enum LbaCompatUtil {
    ;

    public static Content asFluidContent(FluidVolume fluidVolume) {
        return asFluidContent(fluidVolume.getFluidKey());
    }

    public static Content asFluidContent(FluidKey fluidKey) {
        return FluidContent.of(fluidKey.getRawFluid());
    }

    public static FluidKey asFluidKey(Content fluidContent) {
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

    public static Context asContext(Simulation simulation) {
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
