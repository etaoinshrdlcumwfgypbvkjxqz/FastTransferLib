package dev.technici4n.fasttransferlib.impl.base.view;

import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.observer.Subscription;
import dev.technici4n.fasttransferlib.api.view.observer.TransferData;
import dev.technici4n.fasttransferlib.impl.view.observer.FunctionalSubscription;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractView
        implements View {
    private final Set<Consumer<? super TransferData>> observers;
    private Object revision = new Object();

    protected AbstractView() {
        this.observers = new LinkedHashSet<>(2); // todo determine initial capacity
    }

    protected abstract boolean supportsPushNotification();

    protected abstract boolean supportsPullNotification();

    @Override
    public Optional<? extends Subscription> addObserver(Consumer<? super TransferData> observer) {
        if (supportsPushNotification() && getObservers().add(observer))
            return Optional.of(FunctionalSubscription.of(() -> getObservers().remove(observer)));
        return Optional.empty();
    }

    protected Set<Consumer<? super TransferData>> getObservers() {
        return observers;
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
        getObservers().forEach(observer -> observer.accept(data));
    }

    @SuppressWarnings("unused")
    protected void reviseAndNotify(TransferData data) {
        revise();
        notify(data);
    }

    private void setRevision(Object revision) {
        this.revision = revision;
    }
}
