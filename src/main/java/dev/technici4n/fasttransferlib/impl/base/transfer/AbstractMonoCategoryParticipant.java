package dev.technici4n.fasttransferlib.impl.base.transfer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;

public abstract class AbstractMonoCategoryParticipant<T>
        extends AbstractParticipant {
    private final Class<T> category;

    protected AbstractMonoCategoryParticipant(Class<T> category) {
        this.category = category;
    }

    @Override
    public final long insertNonEmpty(Context context, Content content, long maxAmount) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return insertMono(context, content, category.cast(content.getType()), maxAmount);
        return maxAmount;
    }

    @Override
    public final long extractNonEmpty(Context context, Content content, long maxAmount) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return extractMono(context, content, category.cast(content.getType()), maxAmount);
        return 0L;
    }

    protected abstract long insertMono(Context context, Content content, T type, long maxAmount);

    protected abstract long extractMono(Context context, Content content, T type, long maxAmount);

    protected Class<T> getCategory() {
        return category;
    }
}
