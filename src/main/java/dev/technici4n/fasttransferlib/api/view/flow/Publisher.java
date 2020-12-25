package dev.technici4n.fasttransferlib.api.view.flow;

public interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}
