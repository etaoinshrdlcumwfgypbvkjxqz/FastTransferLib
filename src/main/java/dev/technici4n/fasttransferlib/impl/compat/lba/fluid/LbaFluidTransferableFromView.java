package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.util.ViewUtilities;
import net.minecraft.fluid.Fluid;

public class LbaFluidTransferableFromView
        implements FluidTransferable {
    private final View delegate;

    protected LbaFluidTransferableFromView(View delegate) {
        // the use of 'FluidFilter' means a view is required
        this.delegate = delegate;
    }

    public static LbaFluidTransferableFromView of(View delegate) {
        return new LbaFluidTransferableFromView(delegate);
    }

    protected View getDelegate() {
        return delegate;
    }

    @Override
    public FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) {
        Context context = LbaCompatUtil.asContext(simulation);

        long insert = LbaCompatUtil.asAmount(fluid);
        long leftover = ViewUtilities.insert(getDelegate(),
                context,
                LbaCompatUtil.asFluidContent(fluid),
                insert);
        long inserted = insert - leftover;

        return fluid.withAmount(fluid.getAmount_F().sub(LbaCompatUtil.asFluidAmount(inserted)));
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        Context context = LbaCompatUtil.asContext(simulation);

        Content[] contentLock = {null};
        long extracted = ViewUtilities.extract(getDelegate(),
                context,
                LbaCompatUtil.asAmount(maxAmount),
                atom -> {
                    Content content = atom.getContent();
                    if (content.isEmpty() || content.getCategory() != Fluid.class)
                        return null;
                    Content contentLock1 = contentLock[0];
                    if (contentLock1 == null && filter.matches(LbaCompatUtil.asFluidKey(content))) {
                        return contentLock[0] = content;
                    }
                    return contentLock1;
                });

        Content contentLock1 = contentLock[0];
        if (contentLock1 == null)
            return FluidKeys.EMPTY.withAmount(LbaCompatUtil.asFluidAmount(extracted));
        assert contentLock1.getCategory() == Fluid.class;
        return LbaCompatUtil.asFluidVolume(contentLock1, extracted);
    }
}
