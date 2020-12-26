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
    protected long getAmount(Content content, T type) {
        return Atom.super.getAmount(content);
    }

    @Override
    public final long insert(Context context, Content content, long maxAmount) {
        if (content.isEmpty())
            return maxAmount;

        Class<T> category = getCategory();
        if (content.getCategory() != category)
            return maxAmount;

        Content atomContent = getContent();

        if (atomContent.isEmpty())
            return insertNew(context, content, category.cast(content.getType()), maxAmount);
        if (atomContent.equals(content))
            return insertCurrent(context, maxAmount);

        return maxAmount;
    }

    @Override
    public final long extract(Context context, Content content, long maxAmount) {
        if (content.isEmpty())
            return 0L;

        Class<T> category = getCategory();
        if (content.getCategory() != category)
            return 0L;

        Content atomContent = getContent();

        if (atomContent.isEmpty())
            return 0L;
        if (atomContent.equals(content))
            return extractCurrent(context, maxAmount);

        return 0L;
    }

    protected abstract long extractCurrent(Context context, long maxAmount);

    protected abstract long insertCurrent(Context context, long maxAmount);

    protected abstract long insertNew(Context context, Content content, T type, long maxAmount);

    @Override
    public TriState query(Query query) {
        if (query instanceof CategoryQuery && ((CategoryQuery) query).getCategory() != getCategory())
            return TriState.FALSE;
        return TriState.DEFAULT;
    }
}
