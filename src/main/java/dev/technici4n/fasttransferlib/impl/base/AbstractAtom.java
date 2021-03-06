package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.impl.base.view.AbstractView;

public abstract class AbstractAtom
        extends AbstractView
        implements Atom {
    @Override
    public final long insert(Context context, Content content, long maxQuantity) {
        if (content.isEmpty())
            return maxQuantity;

        Content atomContent = getContent();

        if (atomContent.isEmpty())
            return insertNew(context, content, maxQuantity);
        if (atomContent.equals(content))
            return insertCurrent(context, maxQuantity);

        return maxQuantity;
    }

    @Override
    public final long extract(Context context, Content content, long maxQuantity) {
        if (content.isEmpty())
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

    protected abstract long insertNew(Context context, Content content, long maxQuantity);
}
