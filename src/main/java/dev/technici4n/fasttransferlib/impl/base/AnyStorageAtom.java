package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.ContentApi;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.impl.content.EmptyContent;
import net.minecraft.nbt.CompoundTag;

import java.util.OptionalLong;

public class AnyStorageAtom
        implements Atom {
    private Content content = EmptyContent.INSTANCE;
    private final long internalCapacity;
    private long amount;

    public AnyStorageAtom(long capacity) {
        assert capacity >= 0L;
        this.internalCapacity = capacity;
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long insert(Context context, Content content, long maxAmount) {
        Content currentContent = getContent();

        long inserted;
        if (currentContent.isEmpty()) {
            inserted = Math.min(maxAmount, getInternalCapacity());
            context.configure(() -> {
                setContent(content);
                setAmount(inserted);
            }, () -> setAmount(0L));
        } else if (currentContent.equals(content)) {
            long amount = getAmount();
            inserted = Math.min(maxAmount, getInternalCapacity() - amount);
            context.configure(() -> setAmount(amount + inserted), () -> setAmount(amount));
        } else inserted = 0L;

        return maxAmount - inserted;
    }

    @Override
    public long extract(Context context, Content content, long maxAmount) {
        Content currentContent = getContent();

        long extracted;
        if (currentContent.equals(content)) {
            long amount = getAmount();
            extracted = Math.min(maxAmount, amount);
            context.configure(() -> setAmount(amount - extracted), () -> setAmount(amount));
        } else extracted = 0L;

        return extracted;
    }

    protected long getInternalCapacity() {
        return internalCapacity;
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(getInternalCapacity());
    }

    protected void setContent(Content content) {
        this.content = content;
    }

    protected void setAmount(long amount) {
        assert amount >= 0L;
        assert amount <= getInternalCapacity();
        if ((this.amount = amount) == 0L)
            setContent(EmptyContent.INSTANCE);
        else assert !getContent().isEmpty();
    }

    public CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        result.put("content", ContentApi.serialize(getContent()));
        result.putLong("amount", getAmount());
        return result;
    }

    public void fromTag(CompoundTag tag) {
        setContent(ContentApi.deserialize(tag.getCompound("content")));
        setAmount(tag.getLong("amount"));
    }
}
