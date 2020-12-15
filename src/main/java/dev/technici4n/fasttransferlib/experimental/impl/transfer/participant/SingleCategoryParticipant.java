package dev.technici4n.fasttransferlib.experimental.impl.transfer.participant;

import dev.technici4n.fasttransferlib.experimental.api.Instance;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;

public abstract class SingleCategoryParticipant<T>
        implements Participant {
    private final Class<T> category;

    protected SingleCategoryParticipant(Class<T> category) {
        this.category = category;
    }

    @Override
    public final long insert(Context context, Instance instance, long maxAmount) {
        if (instance.getCategory() == category)
            return insert(context, instance, category.cast(instance.getType()), maxAmount);
        return maxAmount;
    }

    @Override
    public final long extract(Context context, Instance instance, long maxAmount) {
        if (instance.getCategory() == category)
            return extract(context, instance, category.cast(instance.getType()), maxAmount);
        return 0L;
    }

    protected abstract long insert(Context context, Instance instance, T type, long maxAmount);

    protected abstract long extract(Context context, Instance instance, T type, long maxAmount);
}
