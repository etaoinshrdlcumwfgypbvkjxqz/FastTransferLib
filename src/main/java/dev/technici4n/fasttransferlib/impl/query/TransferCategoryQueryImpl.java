package dev.technici4n.fasttransferlib.impl.query;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import dev.technici4n.fasttransferlib.api.query.TransferCategoryQuery;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;

import java.util.Arrays;
import java.util.Map;

public class TransferCategoryQueryImpl
        extends TransferQueryImpl
        implements TransferCategoryQuery {
    @SuppressWarnings("UnstableApiUsage")
    private static final Map<TransferAction, LoadingCache<Class<?>, TransferCategoryQueryImpl>> CACHE = Maps.immutableEnumMap(
            Maps.toMap(Arrays.asList(TransferAction.values()),
                    action -> CacheBuilder.newBuilder().concurrencyLevel(1).initialCapacity(4)
                            .build(CacheLoader.from(category -> new TransferCategoryQueryImpl(action, category))))
    );
    private final Class<?> category;

    protected TransferCategoryQueryImpl(TransferAction action, Class<?> category) {
        super(action);
        this.category = category;
    }

    public static TransferCategoryQueryImpl of(TransferAction action, Class<?> category) {
        return CACHE.get(action).getUnchecked(category);
    }

    @Override
    public Class<?> getCategory() {
        return category;
    }
}
