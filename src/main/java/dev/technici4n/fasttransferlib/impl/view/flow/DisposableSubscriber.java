package dev.technici4n.fasttransferlib.impl.view.flow;

import dev.technici4n.fasttransferlib.api.view.flow.Subscriber;
import dev.technici4n.fasttransferlib.api.view.flow.Subscription;

public abstract class DisposableSubscriber<T>
        implements Subscriber<T>, Subscription {
    private Subscription subscription = EmptySubscription.EMPTY;

    @Override
    public final void onSubscribe(Subscription subscription) {
        if (getSubscription() == EmptySubscription.EMPTY) {
            setSubscription(subscription);
            onSubscribe();
        }
    }

    protected abstract void onSubscribe();

    @Override
    public void cancel() {
        Subscription subscription = getSubscription();
        setSubscription(EmptySubscription.CANCELLED);
        subscription.cancel();
    }

    @Override
    public void request(long n) {
        getSubscription().request(n);
    }

    protected Subscription getSubscription() {
        return subscription;
    }

    protected void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
}
