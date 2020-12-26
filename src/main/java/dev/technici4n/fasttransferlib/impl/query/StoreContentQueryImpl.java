package dev.technici4n.fasttransferlib.impl.query;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.query.StoreContentQuery;

public class StoreContentQueryImpl
        extends StoreCategoryQueryImpl
        implements StoreContentQuery {
    private final Content content;

    protected StoreContentQueryImpl(Content content) {
        super(content.getCategory());
        this.content = content;
    }

    public static StoreContentQueryImpl of(Content content) {
        return new StoreContentQueryImpl(content);
    }

    @Override
    public Content getContent() {
        return content;
    }
}
