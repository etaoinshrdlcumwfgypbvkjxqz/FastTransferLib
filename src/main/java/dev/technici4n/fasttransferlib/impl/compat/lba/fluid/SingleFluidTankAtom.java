package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.SingleFluidTankView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatImplUtil;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import net.minecraft.fluid.Fluid;

import java.util.OptionalLong;

public class SingleFluidTankAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private final SingleFluidTankView delegate;

    protected SingleFluidTankAtom(SingleFluidTankView delegate) {
        super(Fluid.class);
        this.delegate = delegate;
    }

    public static SingleFluidTankAtom of(SingleFluidTankView delegate) {
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
    public OptionalLong getCapacity() {
        return OptionalLong.of(LbaCompatUtil.asAmount(getDelegate().getMaxAmount_F()));
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        return LbaCompatImplUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        return LbaCompatImplUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
    }

    protected SingleFluidTankView getDelegate() {
        return delegate;
    }
}
