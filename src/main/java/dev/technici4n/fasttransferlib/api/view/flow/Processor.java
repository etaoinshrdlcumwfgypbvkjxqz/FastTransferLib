package dev.technici4n.fasttransferlib.api.view.flow;

public interface Processor<T, R>
        extends Subscriber<T>, Publisher<R> {}
