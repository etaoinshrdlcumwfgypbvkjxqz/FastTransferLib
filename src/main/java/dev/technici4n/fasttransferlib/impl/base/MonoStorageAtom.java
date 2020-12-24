package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import net.minecraft.nbt.CompoundTag;

public class MonoStorageAtom<T>
        extends AbstractMonoCategoryAtom<T> {
    private final AnyStorageAtom delegate;

    public MonoStorageAtom(Class<T> category, long capacity) {
        super(category);
        this.delegate = new AnyStorageAtom(capacity);
    }

    public CompoundTag toTag() {
        return getDelegate().toTag();
    }

    public void fromTag(CompoundTag tag) {
        getDelegate().fromTag(tag);
    }

    protected AnyStorageAtom getDelegate() {
        return delegate;
    }

    @Override
    public Content getContent() {
        return getDelegate().getContent();
    }

    @Override
    public long getAmount() {
        return getDelegate().getAmount();
    }

    @Override
    protected long insert(Context context, Content content, T type, long maxAmount) {
        // already filtered
        return getDelegate().insert(context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, T type, long maxAmount) {
        // already filtered
        return getDelegate().extract(context, content, maxAmount);
    }
}
