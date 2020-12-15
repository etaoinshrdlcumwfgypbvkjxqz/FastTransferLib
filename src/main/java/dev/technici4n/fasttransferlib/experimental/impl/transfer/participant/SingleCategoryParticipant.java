package dev.technici4n.fasttransferlib.experimental.impl.transfer.participant;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;

public abstract class SingleCategoryParticipant<T>
        implements Participant {
    private final Class<T> category;

    protected SingleCategoryParticipant(Class<T> category) {
        this.category = category;
    }

    @Override
    public final long insert(Context context, Content content, long maxAmount) {
        if (content.getCategory() == category)
            return insert(context, content, category.cast(content.getType()), maxAmount);
        return maxAmount;
    }

    @Override
    public final long extract(Context context, Content content, long maxAmount) {
        if (content.getCategory() == category)
            return extract(context, content, category.cast(content.getType()), maxAmount);
        return 0L;
    }

    protected abstract long insert(Context context, Content content, T type, long maxAmount);

    protected abstract long extract(Context context, Content content, T type, long maxAmount);
}
