package dev.technici4n.fasttransferlib.api.view.flow;

// stripped-down Reactive Streams/java.util.concurrent.Flow in JDJ9
public interface Subscriber<T> {
    void onSubscribe(Subscription subscription);

    void onNext(T t);

    void onError(Throwable throwable);

    void onComplete();
}
