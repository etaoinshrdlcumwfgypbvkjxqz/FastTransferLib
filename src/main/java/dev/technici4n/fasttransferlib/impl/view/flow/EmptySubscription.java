package dev.technici4n.fasttransferlib.impl.view.flow;

import dev.technici4n.fasttransferlib.api.view.flow.Subscription;

public enum EmptySubscription
        implements Subscription {
    EMPTY,
    CANCELLED,
    ;

    @Override
    public void request(long n) {
        // NOOP
    }

    @Override
    public void cancel() {
        // NOOP
    }
}
