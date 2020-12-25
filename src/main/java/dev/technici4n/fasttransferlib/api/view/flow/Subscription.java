package dev.technici4n.fasttransferlib.api.view.flow;

public interface Subscription {
    long UNBOUNDED_REQUEST = Long.MAX_VALUE;

    void request(long n);

    void cancel();
}
