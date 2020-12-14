package dev.technici4n.fasttransferlib.experimental.transfer.impl.participant;

import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Participant;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Transferable;

public abstract class SingleCategoryParticipant<T>
        implements Participant {
    private final Class<T> category;

    protected SingleCategoryParticipant(Class<T> category) {
        this.category = category;
    }

    @Override
    public final long insert(Context context, Transferable transferable, long maxAmount) {
        if (transferable.getCategory() == category)
            return insert(context, transferable, category.cast(transferable.getType()), maxAmount);
        return maxAmount;
    }

    @Override
    public final long extract(Context context, Transferable transferable, long maxAmount) {
        if (transferable.getCategory() == category)
            return extract(context, transferable, category.cast(transferable.getType()), maxAmount);
        return 0L;
    }

    protected abstract long insert(Context context, Transferable transferable, T type, long maxAmount);

    protected abstract long extract(Context context, Transferable transferable, T type, long maxAmount);
}
