package dev.technici4n.fasttransferlib.impl.base;

import com.google.common.collect.ImmutableSet;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.ContentApi;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.impl.view.flow.TransferDataImpl;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.Set;

public class AnyStorageAtom
        extends AbstractAtom {
    private static final Set<Class<?>> SUPPORTED_PUSH_NOTIFICATIONS = ImmutableSet.of(TransferData.class);
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
    protected long extractCurrent(Context context, long maxAmount) {
        long amount = getAmount();
        long extracted = Math.min(maxAmount, amount);
        if (extracted == 0L)
            return 0L;

        Content content = getContent();
        context.configure(() -> setAmount(amount - extracted), () -> setAmount(amount));
        context.execute(() -> reviseAndNotify(TransferData.class, TransferDataImpl.ofExtraction(content, extracted)));
        return extracted;
    }

    @Override
    protected long insertCurrent(Context context, long maxAmount) {
        long amount = getAmount();
        long inserted = Math.min(maxAmount, getInternalCapacity() - amount);
        if (inserted == 0L)
            return maxAmount;

        Content content = getContent();
        context.configure(() -> setAmount(amount + inserted), () -> setAmount(amount));
        context.execute(() -> reviseAndNotify(TransferData.class, TransferDataImpl.ofInsertion(content, inserted)));
        return maxAmount - inserted;
    }

    @Override
    protected long insertNew(Context context, Content content, long maxAmount) {
        long inserted = Math.min(maxAmount, getInternalCapacity());
        if (inserted == 0L)
            return maxAmount;

        context.configure(() -> {
            setContent(content);
            setAmount(inserted);
        }, () -> setAmount(0L));
        context.execute(() -> reviseAndNotify(TransferData.class, TransferDataImpl.ofInsertion(content, inserted)));
        return maxAmount - inserted;
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

    @Override
    protected Collection<? extends Class<?>> getSupportedPushNotifications() {
        return SUPPORTED_PUSH_NOTIFICATIONS;
    }

    @Override
    protected boolean supportsPullNotification() {
        return true;
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
