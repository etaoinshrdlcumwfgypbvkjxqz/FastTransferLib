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
    private final LoadingCache<Class<?>, Object> revisions;
    private final LoadingCache<Class<?>, EmittingPublisher<?>> publishers;

    protected AbstractView() {
        this.revisions = CacheBuilder.newBuilder().concurrencyLevel(1).initialCapacity(4)
                .build(CacheLoader.from(Object::new));
        this.publishers = CacheBuilder.newBuilder().concurrencyLevel(1).initialCapacity(4)
                .build(CacheLoader.from(key -> new EmittingPublisher<>(4)));
    }

    protected abstract Collection<? extends Class<?>> getSupportedPushEvents();

    protected abstract Collection<? extends Class<?>> getSupportedPullEvents();

    @Override
    public Object getRevisionFor(Class<?> event) {
        if (getSupportedPullEvents().contains(event))
            return getRevisions().getUnchecked(event);
        return new Object();
    }

    protected void revise(Class<?> event) {
        assert getSupportedPullEvents().contains(event);
        getRevisions().refresh(event);
    }

    protected <T> void notify(Class<T> event, T data) {
        assert getSupportedPushEvents().contains(event);
        getPublisherIfPresent(event).ifPresent(publisher -> publisher.emit(data));
    }

    @SuppressWarnings("unused")
    protected <T> void reviseAndNotify(@SuppressWarnings("SameParameterValue") Class<T> event, T data) {
        revise(event);
        notify(event, data);
    }

    @Override
    public <T> Optional<? extends Publisher<T>> getPublisherFor(Class<T> event) {
        if (getSupportedPushEvents().contains(event))
            return Optional.of(getPublisher(event));
        return Optional.empty();
    }

    protected LoadingCache<Class<?>, Object> getRevisions() {
        return revisions;
    }

    protected LoadingCache<Class<?>, EmittingPublisher<?>> getPublishers() {
        return publishers;
    }

    @SuppressWarnings("unchecked")
    protected <T> EmittingPublisher<T> getPublisher(Class<T> event) {
        return (EmittingPublisher<T>) getPublishers().getUnchecked(event);
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<? extends EmittingPublisher<T>> getPublisherIfPresent(Class<T> event) {
        return Optional.ofNullable((EmittingPublisher<T>) getPublishers().getIfPresent(event));
    }
}
