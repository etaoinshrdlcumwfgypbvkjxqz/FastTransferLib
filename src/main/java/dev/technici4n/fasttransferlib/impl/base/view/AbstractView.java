package dev.technici4n.fasttransferlib.impl.base.view;

import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.flow.Subscriber;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.view.observer.FunctionalSubscription;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractView
        implements View {
    private final Set<Subscriber<? super TransferData>> subscribers;
    private Object revision = new Object();

    protected AbstractView() {
        this.subscribers = new LinkedHashSet<>(2); // todo determine initial capacity
    }

    protected abstract boolean supportsPushNotification();

    protected abstract boolean supportsPullNotification();

    @Override
    public boolean subscribe(Subscriber<? super TransferData> subscriber) {
        if (supportsPushNotification() && getSubscribers().add(subscriber)) {
            subscriber.onSubscribe(FunctionalSubscription.of(() -> {
                getSubscribers().remove(subscriber);
                subscriber.onComplete();
            }));
            return true;
        }
        return false;
    }

    protected Set<Subscriber<? super TransferData>> getSubscribers() {
        return subscribers;
    }

    @Override
    public Object getRevision() {
        if (supportsPullNotification())
            return revision;
        return new Object();
    }

    protected void revise() {
        assert supportsPullNotification();
        setRevision(new Object());
    }

    protected void notify(TransferData data) {
        assert supportsPushNotification();
        getSubscribers().forEach(observer -> observer.onNext(data));
    }

    @SuppressWarnings("unused")
    protected void reviseAndNotify(TransferData data) {
        revise();
        notify(data);
    }

    protected void clearSubscribers() {
        for (Iterator<Subscriber<? super TransferData>> iterator = getSubscribers().iterator(); iterator.hasNext(); ) {
            Subscriber<?> subscriber = iterator.next();
            iterator.remove();
            subscriber.onComplete();
        }
    }

    protected void setRevision(Object revision) {
        this.revision = revision;
    }
}
