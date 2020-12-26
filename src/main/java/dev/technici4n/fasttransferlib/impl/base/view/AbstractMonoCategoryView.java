package dev.technici4n.fasttransferlib.impl.base.view;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.query.StoreCategoryQuery;
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
    public final long getAmount(Content content) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return getAmount(content, category.cast(content.getType()));
        return 0L;
    }

    protected abstract long getAmount(Content content, T type);

    @Override
    public TriState query(Query query) {
        if (query instanceof StoreCategoryQuery && ((StoreCategoryQuery) query).getCategory() != getCategory())
            return TriState.FALSE;
        return TriState.DEFAULT;
    }
}
