package dev.technici4n.fasttransferlib.impl.view.observer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.observer.TransferData;

public final class TransferDataImpl
        implements TransferData {
    private final Type type;
    private final Content content;
    private final long amount;

    private TransferDataImpl(Type type, Content content, long amount) {
        this.type = type;
        this.content = content;
        this.amount = amount;
    }

    public static TransferDataImpl of(Type type, Content content, long amount) {
        return new TransferDataImpl(type, content, amount);
    }

    public static TransferDataImpl ofInsertion(Content content, long amount) {
        return of(Type.INSERT, content, amount);
    }

    public static TransferDataImpl ofExtraction(Content content, long amount) {
        return of(Type.EXTRACT, content, amount);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public long getAmount() {
        return amount;
    }
}
