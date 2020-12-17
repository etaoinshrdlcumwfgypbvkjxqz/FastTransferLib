package dev.technici4n.fasttransferlib.experimental.impl.base;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.FluidContent;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MonoFluidStorage
        extends AbstractMonoCategoryAtom<Fluid> {
    private Content fluidContent = EmptyContent.INSTANCE;
    private final long capacity;
    private long amount;

    public MonoFluidStorage(long capacity) {
        super(Fluid.class);
        assert capacity >= 0L;
        this.capacity = capacity;
    }

    @Override
    public Content getContent() {
        return getFluidContent();
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    protected long insert(Context context, Content content, Fluid type, long maxAmount) {
        Content fluidContent = getFluidContent();

        long inserted;
        if (fluidContent.isEmpty()) {
            inserted = Math.min(maxAmount, getCapacity());
            context.execute(() -> {
                setFluidContent(content);
                setAmount(inserted);
            }, () -> setAmount(0L));
        } else if (fluidContent.equals(content)) {
            long amount = getAmount();
            inserted = Math.min(maxAmount, getCapacity() - amount);
            context.execute(() -> setAmount(amount + inserted), () -> setAmount(amount));
        } else inserted = 0L;

        return maxAmount - inserted;
    }

    @Override
    protected long extract(Context context, Content content, Fluid type, long maxAmount) {
        Content fluidContent = getFluidContent();

        long extracted;
        if (fluidContent.equals(content)) {
            long amount = getAmount();
            extracted = Math.min(maxAmount, amount);
            context.execute(() -> setAmount(amount - extracted), () -> setAmount(amount));
        } else extracted = 0L;

        return extracted;
    }

    protected long getCapacity() {
        return capacity;
    }

    protected Content getFluidContent() {
        return fluidContent;
    }

    protected void setFluidContent(Content fluidContent) {
        assert fluidContent.getCategory() == Fluid.class || fluidContent.isEmpty();
        this.fluidContent = fluidContent;
    }

    protected void setAmount(long amount) {
        assert amount >= 0L;
        assert amount <= getCapacity();
        if ((this.amount = amount) == 0L)
            setFluidContent(EmptyContent.INSTANCE);
        else assert !getFluidContent().isEmpty();
    }

    public CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        Content fluidContent = getFluidContent();
        if (!fluidContent.isEmpty())
            result.putString("fluidContent",  Registry.FLUID.getId((Fluid) fluidContent.getType()).toString());
        result.putLong("amount", getAmount());
        return result;
    }

    public void fromTag(CompoundTag tag) {
        if (tag.contains("fluidContent"))
            setFluidContent(FluidContent.of(Registry.FLUID.get(new Identifier(tag.getString("fluidContent")))));
        else
            setFluidContent(EmptyContent.INSTANCE);
        setAmount(tag.getLong("amount"));
    }
}
