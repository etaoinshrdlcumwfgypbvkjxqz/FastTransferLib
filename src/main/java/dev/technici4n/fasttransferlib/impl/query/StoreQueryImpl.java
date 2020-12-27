package dev.technici4n.fasttransferlib.impl.query;

import dev.technici4n.fasttransferlib.api.query.StoreQuery;

public class StoreQueryImpl
        implements StoreQuery {
    private static final StoreQueryImpl INSTANCE = new StoreQueryImpl();

    protected StoreQueryImpl() {}

    public static StoreQueryImpl of() {
        return INSTANCE;
    }
}
