package dev.technici4n.fasttransferlib.impl.query;

import com.google.common.collect.Maps;
import dev.technici4n.fasttransferlib.api.query.TransferQuery;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;

import java.util.Arrays;
import java.util.Map;

public class TransferQueryImpl
        implements TransferQuery {
    @SuppressWarnings({"UnstableApiUsage", "ConstantConditions"})
    private static final Map<TransferAction, TransferQueryImpl> CACHE = Maps.immutableEnumMap(
            Maps.toMap(Arrays.asList(TransferAction.values()),
            TransferQueryImpl::new)
    );
    private final TransferAction action;

    protected TransferQueryImpl(TransferAction action) {
        this.action = action;
    }

    public static TransferQueryImpl of(TransferAction action) {
        return CACHE.get(action);
    }

    @Override
    public TransferAction getAction() {
        return action;
    }
}
