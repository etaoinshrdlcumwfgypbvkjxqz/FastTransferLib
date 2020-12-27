package dev.technici4n.fasttransferlib.impl.base.transfer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.CategoryQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import net.fabricmc.fabric.api.util.TriState;

public abstract class AbstractMonoCategoryParticipant<T>
        extends AbstractParticipant {
    private final Class<T> category;

    protected AbstractMonoCategoryParticipant(Class<T> category) {
        this.category = category;
    }

    @Override
    public final long insertNonEmpty(Context context, Content content, long maxQuantity) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return insertMono(context, content, category.cast(content.getType()), maxQuantity);
        return maxQuantity;
    }

    @Override
    public final long extractNonEmpty(Context context, Content content, long maxQuantity) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return extractMono(context, content, category.cast(content.getType()), maxQuantity);
        return 0L;
    }

    protected abstract long insertMono(Context context, Content content, T type, long maxQuantity);

    protected abstract long extractMono(Context context, Content content, T type, long maxQuantity);

    protected Class<T> getCategory() {
        return category;
    }

    @Override
    public TriState query(Query query) {
        if (query instanceof StoreQuery) {
            if (query instanceof CategoryQuery && ((CategoryQuery) query).getCategory() != getCategory())
                return TriState.FALSE;
        }
        return TriState.DEFAULT;
    }
}
