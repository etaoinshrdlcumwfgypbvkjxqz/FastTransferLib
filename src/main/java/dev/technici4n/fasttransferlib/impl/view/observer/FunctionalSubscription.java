package dev.technici4n.fasttransferlib.impl.view.observer;

import dev.technici4n.fasttransferlib.api.view.flow.Subscription;

public class FunctionalSubscription
        implements Subscription {
    private final Runnable cancelAction;

    protected FunctionalSubscription(Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    public static FunctionalSubscription of(Runnable cancelAction) {
        return new FunctionalSubscription(cancelAction);
    }

    @Override
    public void cancel() {
        getCancelAction().run();
    }

    protected Runnable getCancelAction() {
        return cancelAction;
    }
}
