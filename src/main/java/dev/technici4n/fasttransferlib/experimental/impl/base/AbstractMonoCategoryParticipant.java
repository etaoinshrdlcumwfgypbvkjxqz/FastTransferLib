package dev.technici4n.fasttransferlib.experimental.impl.base;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;

public abstract class AbstractMonoCategoryParticipant<T>
        implements Participant {
    private final Class<T> category;

    protected AbstractMonoCategoryParticipant(Class<T> category) {
        this.category = category;
    }

    @Override
    public final long insert(Context context, Content content, long maxAmount) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return insert(context, content, category.cast(content.getType()), maxAmount);
        return maxAmount;
    }

    @Override
    public final long extract(Context context, Content content, long maxAmount) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return extract(context, content, category.cast(content.getType()), maxAmount);
        return 0L;
    }

    protected abstract long insert(Context context, Content content, T type, long maxAmount);

    protected abstract long extract(Context context, Content content, T type, long maxAmount);

    protected Class<T> getCategory() {
        return category;
    }
}
