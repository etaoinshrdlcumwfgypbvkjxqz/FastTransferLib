package dev.technici4n.fasttransferlib.impl.base.view;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.query.CategoryQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreQuery;
import net.fabricmc.fabric.api.util.TriState;

public abstract class AbstractMonoCategoryView<T>
        extends AbstractView {
    private final Class<T> category;

    protected AbstractMonoCategoryView(Class<T> category) {
        this.category = category;
    }

    protected Class<T> getCategory() {
        return category;
    }

    @Override
    public final long getQuantity(Content content) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return getQuantity(content, category.cast(content.getType()));
        return 0L;
    }

    protected abstract long getQuantity(Content content, T type);

    @Override
    public TriState query(Query query) {
        if (query instanceof StoreQuery) {
            if (query instanceof CategoryQuery && ((CategoryQuery) query).getCategory() != getCategory())
                return TriState.FALSE;
        }
        return TriState.DEFAULT;
    }
}
