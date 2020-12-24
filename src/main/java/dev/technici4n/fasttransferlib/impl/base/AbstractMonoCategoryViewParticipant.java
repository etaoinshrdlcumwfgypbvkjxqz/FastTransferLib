package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.View;

public abstract class AbstractMonoCategoryViewParticipant<T>
        extends AbstractMonoCategoryParticipant<T>
        implements View {
    protected AbstractMonoCategoryViewParticipant(Class<T> category) {
        super(category);
    }

    @Override
    public final long getAmount(Content content) {
        Class<T> category = getCategory();
        if (content.getCategory() == category)
            return getAmount(content, category.cast(content.getType()));
        return 0L;
    }

    protected abstract long getAmount(Content content, T type);
}
