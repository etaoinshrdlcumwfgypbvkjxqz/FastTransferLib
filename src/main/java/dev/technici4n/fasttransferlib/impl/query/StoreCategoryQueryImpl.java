package dev.technici4n.fasttransferlib.impl.query;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.api.query.CategoryQuery;

public class StoreCategoryQueryImpl
        extends StoreQueryImpl
        implements CategoryQuery {
    private static final LoadingCache<Class<?>, StoreCategoryQueryImpl> CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).initialCapacity(4)
                            .build(CacheLoader.from(StoreCategoryQueryImpl::new));
    private final Class<?> category;

    protected StoreCategoryQueryImpl(Class<?> category) {
        this.category = category;
    }

    public static StoreCategoryQueryImpl of(Class<?> category) {
        return CACHE.getUnchecked(category);
    }

    @Override
    public Class<?> getCategory() {
        return category;
    }
}
