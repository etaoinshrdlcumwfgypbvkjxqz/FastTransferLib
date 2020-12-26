package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.CategoryQuery;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractMonoCategoryView;
import net.fabricmc.fabric.api.util.TriState;

public abstract class AbstractMonoCategoryAtom<T>
        extends AbstractMonoCategoryView<T>
        implements Atom {
    protected AbstractMonoCategoryAtom(Class<T> category) {
        super(category);
    }

    @Override
    protected long getQuantity(Content content, T type) {
        return Atom.super.getQuantity(content);
    }

    @Override
    public final long insert(Context context, Content content, long maxQuantity) {
        if (content.isEmpty())
            return maxQuantity;

        Class<T> category = getCategory();
        if (content.getCategory() != category)
            return maxQuantity;

        Content atomContent = getContent();

        if (atomContent.isEmpty())
            return insertNew(context, content, category.cast(content.getType()), maxQuantity);
        if (atomContent.equals(content))
            return insertCurrent(context, maxQuantity);

        return maxQuantity;
    }

    @Override
    public final long extract(Context context, Content content, long maxQuantity) {
        if (content.isEmpty())
            return 0L;

        Class<T> category = getCategory();
        if (content.getCategory() != category)
            return 0L;

        Content atomContent = getContent();

        if (atomContent.isEmpty())
            return 0L;
        if (atomContent.equals(content))
            return extractCurrent(context, maxQuantity);

        return 0L;
    }

    protected abstract long extractCurrent(Context context, long maxQuantity);

    protected abstract long insertCurrent(Context context, long maxQuantity);

    protected abstract long insertNew(Context context, Content content, T type, long maxQuantity);

    @Override
    public TriState query(Query query) {
        if (query instanceof CategoryQuery && ((CategoryQuery) query).getCategory() != getCategory())
            return TriState.FALSE;
        return TriState.DEFAULT;
    }
}
