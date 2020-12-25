package dev.technici4n.fasttransferlib.impl.view.flow;

import dev.technici4n.fasttransferlib.api.view.flow.Publisher;
import dev.technici4n.fasttransferlib.api.view.flow.Subscriber;
import dev.technici4n.fasttransferlib.api.view.flow.Subscription;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Iterator;

public class EmittingPublisher<T>
        implements Publisher<T> {
    public static final long DEFAULT_RETURN_VALUE = -1L;
    private final Object2LongMap<Subscriber<? super T>> subscribers;

    public EmittingPublisher(int expected) {
        this.subscribers = new Object2LongOpenHashMap<>(expected);
        this.subscribers.defaultReturnValue(DEFAULT_RETURN_VALUE);
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        getSubscribers().put(subscriber, 0L);
        subscriber.onSubscribe(new SubscriptionImpl(subscriber));
    }

    public void emit(T t) {
        getSubscribers().object2LongEntrySet().forEach(entry -> {
            long requested = entry.getLongValue();
            if (requested > 0L) {
                if (requested != Subscription.UNBOUNDED_REQUEST)
                    entry.setValue(requested - 1L);
                entry.getKey().onNext(t);
            }
        });
    }

    @SuppressWarnings("unused")
    public void clearSubscribers() {
        for (Iterator<Subscriber<? super T>> iterator = getSubscribers().keySet().iterator(); iterator.hasNext(); ) {
            Subscriber<?> subscriber = iterator.next();
            iterator.remove();
            subscriber.onComplete();
        }
    }

    @SuppressWarnings("unused")
    public void clearSubscribers(Throwable throwable) {
        for (Iterator<Subscriber<? super T>> iterator = getSubscribers().keySet().iterator(); iterator.hasNext(); ) {
            Subscriber<?> subscriber = iterator.next();
            iterator.remove();
            subscriber.onError(throwable);
        }
    }

    protected void unsubscribe(Subscriber<? super T> subscriber) {
        if (getSubscribers().removeLong(subscriber) != DEFAULT_RETURN_VALUE)
            subscriber.onComplete();
    }

    protected Object2LongMap<Subscriber<? super T>> getSubscribers() {
        return subscribers;
    }

    public class SubscriptionImpl
            implements Subscription {
        private final Subscriber<? super T> subscriber;

        public SubscriptionImpl(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            assert n >= 0L;
            getSubscribers().computeLongIfPresent(getSubscriber(), n == UNBOUNDED_REQUEST
                    ? (key, value) -> UNBOUNDED_REQUEST
                    : (key, value) -> {
                if (value == UNBOUNDED_REQUEST)
                    return UNBOUNDED_REQUEST;
                long result = value + n;
                if (result < value) // overflow
                    return UNBOUNDED_REQUEST;
                return result;
            });
        }

        @Override
        public void cancel() {
            unsubscribe(getSubscriber());
        }

        protected Subscriber<? super T> getSubscriber() {
            return subscriber;
        }
    }
}
