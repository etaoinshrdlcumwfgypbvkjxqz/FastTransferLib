package dev.technici4n.fasttransferlib.impl.base;

import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.ContentApi;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.event.CapacityChangeEvent;
import dev.technici4n.fasttransferlib.api.view.event.NetTransferEvent;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;
import dev.technici4n.fasttransferlib.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.impl.view.event.TransferEventImpl;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class AnyStorageAtom
        extends AbstractAtom {
    private static final Set<Class<?>> SUPPORTED_PUSH_EVENTS = ImmutableSet.of(TransferEvent.class, CapacityChangeEvent.class);
    private static final Set<Class<?>> SUPPORTED_PULL_EVENTS = ImmutableSet.of(TransferEvent.class, NetTransferEvent.class, CapacityChangeEvent.class);
    private Content content = EmptyContent.INSTANCE;
    private final long internalCapacity;
    private long quantity;

    public AnyStorageAtom(long capacity) {
        assert capacity >= 0L;
        this.internalCapacity = capacity;
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public long getQuantity() {
        return quantity;
    }

    @Override
    protected long extractCurrent(Context context, long maxQuantity) {
        long quantity = getQuantity();
        long extracted = Math.min(maxQuantity, quantity);
        if (extracted == 0L)
            return 0L;

        Content content = getContent();
        context.configure(() -> setQuantity(quantity - extracted), () -> setQuantity(quantity));
        context.execute(() -> reviseAndNotify(TransferEvent.class, TransferEventImpl.ofExtraction(content, extracted)));
        return extracted;
    }

    @Override
    protected long insertCurrent(Context context, long maxQuantity) {
        long quantity = getQuantity();
        long inserted = Math.min(maxQuantity, getInternalCapacity() - quantity);
        if (inserted == 0L)
            return maxQuantity;

        Content content = getContent();
        context.configure(() -> setQuantity(quantity + inserted), () -> setQuantity(quantity));
        context.execute(() -> {
            revise(NetTransferEvent.class);
            reviseAndNotify(TransferEvent.class, TransferEventImpl.ofInsertion(content, inserted));
        });
        return maxQuantity - inserted;
    }

    @Override
    protected long insertNew(Context context, Content content, long maxQuantity) {
        long inserted = Math.min(maxQuantity, getInternalCapacity());
        if (inserted == 0L)
            return maxQuantity;

        context.configure(() -> {
            setContent(content);
            setQuantity(inserted);
        }, () -> setQuantity(0L));
        context.execute(() -> reviseAndNotify(TransferEvent.class, TransferEventImpl.ofInsertion(content, inserted)));
        return maxQuantity - inserted;
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

    protected void setQuantity(long quantity) {
        assert quantity >= 0L;
        assert quantity <= getInternalCapacity();
        if ((this.quantity = quantity) == 0L)
            setContent(EmptyContent.INSTANCE);
        else assert !getContent().isEmpty();
    }

    public CompoundTag toTag() {
        CompoundTag result = new CompoundTag();
        result.put("content", ContentApi.serialize(getContent()));
        result.putLong("quantity", getQuantity());
        return result;
    }

    public void fromTag(CompoundTag tag) {
        setContent(ContentApi.deserialize(tag.getCompound("content")));
        setQuantity(tag.getLong("quantity"));
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPushEvents() {
        // fixed capacity
        return SUPPORTED_PUSH_EVENTS;
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPullEvents() {
        // fixed capacity
        return SUPPORTED_PULL_EVENTS;
    }

    @Override
    public TriState query(Query query) {
        if (query instanceof TransferQuery)
            return TriState.TRUE;
        if (query instanceof StoreQuery)
            return TriState.TRUE;
        return TriState.DEFAULT;
    }
}
