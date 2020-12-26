package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.impl.util.TriStateUtilities;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.OptionalLong;

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
    public long getQuantity() {
        return getDelegate().getQuantity();
    }

    @Override
    public OptionalLong getCapacity() {
        return getDelegate().getCapacity();
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        // already filtered
        return getDelegate().extractCurrent(context, maxQuantity);
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        // already filtered
        return getDelegate().insertCurrent(context, maxQuantity);
    }

    @Override
    protected long insertNew(Context context, Content content, T type, long maxQuantity) {
        // already filtered
        return getDelegate().insertNew(context, content, maxQuantity);
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPushEvents() {
        return getDelegate().getSupportedPushEvents();
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPullEvents() {
        return getDelegate().getSupportedPullEvents();
    }

    @Override
    public TriState query(Query query) {
        return TriStateUtilities.orGet(super.query(query), () -> getDelegate().query(query));
    }
}
