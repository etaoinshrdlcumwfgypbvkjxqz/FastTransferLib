package dev.technici4n.fasttransferlib.api.view.flow;

// can be directly replaced with Reactive Streams/java.util.concurrent.Flow in JDK9
public interface Subscriber<T> {
    void onSubscribe(Subscription subscription);

    void onNext(T t);

    void onError(Throwable throwable);

    void onComplete();
}
