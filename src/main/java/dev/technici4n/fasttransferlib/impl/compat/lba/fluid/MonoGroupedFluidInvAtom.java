package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatImplUtil;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import net.minecraft.fluid.Fluid;

import java.util.OptionalLong;

public class MonoGroupedFluidInvAtom
        extends AbstractMonoCategoryAtom<Fluid> {
    private final GroupedFluidInvView delegate;
    private final Content content;
    private final FluidKey key;

    protected MonoGroupedFluidInvAtom(GroupedFluidInvView delegate, Content content) {
        super(Fluid.class);
        assert content.getCategory() == Fluid.class;

        this.delegate = delegate;
        this.content = content;
        this.key = FluidKeys.get((Fluid) content.getType());
    }

    public static MonoGroupedFluidInvAtom of(GroupedFluidInvView delegate, Content content) {
        return new MonoGroupedFluidInvAtom(delegate, content);
    }

    protected GroupedFluidInvView getDelegate() {
        return delegate;
    }

    protected FluidKey getKey() {
        return key;
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public long getAmount() {
        return LbaCompatUtil.asAmount(getDelegate().getAmount_F(getKey()));
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(LbaCompatUtil.asAmount(getDelegate().getCapacity_F(getKey())));
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        if (content.equals(getContent()))
            return LbaCompatImplUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
        return maxAmount;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        if (content.equals(getContent()))
            return LbaCompatImplUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
        return 0L;
    }
}
