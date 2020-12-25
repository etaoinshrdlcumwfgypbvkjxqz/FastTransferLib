package dev.technici4n.fasttransferlib.impl.base.view;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.flow.Publisher;
import dev.technici4n.fasttransferlib.impl.view.flow.EmittingPublisher;

import java.util.Collection;
import java.util.Optional;

public abstract class AbstractView
        implements View {
    private Object revision;
    private final LoadingCache<Class<?>, EmittingPublisher<?>> publishers;

    protected AbstractView() {
        this.revision = new Object();
        this.publishers = CacheBuilder.newBuilder().concurrencyLevel(1).initialCapacity(4)
                .build(CacheLoader.from(key -> new EmittingPublisher<>(4)));
    }

    protected abstract Collection<? extends Class<?>> getSupportedPushNotifications();

    protected abstract boolean supportsPullNotification();

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

    protected <T> void notify(Class<T> discriminator, T data) {
        assert getSupportedPushNotifications().contains(discriminator);
        getPublisherIfPresent(discriminator).ifPresent(publisher -> publisher.emit(data));
    }

    @SuppressWarnings("unused")
    protected <T> void reviseAndNotify(Class<T> discriminator, T data) {
        revise();
        notify(discriminator, data);
    }

    protected void setRevision(Object revision) {
        this.revision = revision;
    }

    @Override
    public <T> Optional<? extends Publisher<T>> getPublisher(Class<T> discriminator) {
        if (getSupportedPushNotifications().contains(discriminator))
            return Optional.of(getPublisherUnchecked(discriminator));
        return Optional.empty();
    }

    protected LoadingCache<Class<?>, EmittingPublisher<?>> getPublishers() {
        return publishers;
    }

    @SuppressWarnings("unchecked")
    protected <T> EmittingPublisher<T> getPublisherUnchecked(Class<T> discriminator) {
        return (EmittingPublisher<T>) getPublishers().getUnchecked(discriminator);
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<? extends EmittingPublisher<T>> getPublisherIfPresent(Class<T> discriminator) {
        return Optional.ofNullable((EmittingPublisher<T>) getPublishers().getIfPresent(discriminator));
    }
}
