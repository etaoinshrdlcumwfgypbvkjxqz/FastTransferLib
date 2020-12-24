package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.SingleFluidTankView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import net.minecraft.fluid.Fluid;

public class SingleFluidTankAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private final SingleFluidTank delegate;

    protected SingleFluidTankAtom(SingleFluidTank delegate) {
        super(Fluid.class);
        this.delegate = delegate;
    }

    public static SingleFluidTankAtom of(SingleFluidTank delegate) {
        return new SingleFluidTankAtom(delegate);
    }

    @Override
    public Content getContent() {
        return LbaCompatUtil.asFluidContent(getDelegate().get());
    }

    @Override
    public long getAmount() {
        return LbaCompatUtil.asAmount(getDelegate().get());
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        return insertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        return extractImpl(getDelegate(), context, content, maxAmount);
    }

    protected SingleFluidTank getDelegate() {
        return delegate;
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
