package dev.technici4n.fasttransferlib.impl.view.event;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;

public final class TransferEventImpl
        implements TransferEvent {
    private final TransferAction action;
    private final Content content;
    private final long amount;

    private TransferEventImpl(TransferAction action, Content content, long amount) {
        this.action = action;
        this.content = content;
        this.amount = amount;
    }

    public static TransferEventImpl of(TransferAction action, Content content, long amount) {
        return new TransferEventImpl(action, content, amount);
    }

    public static TransferEventImpl ofInsertion(Content content, long amount) {
        return of(TransferAction.INSERT, content, amount);
    }

    public static TransferEventImpl ofExtraction(Content content, long amount) {
        return of(TransferAction.EXTRACT, content, amount);
    }

    @Override
    public TransferAction getType() {
        return action;
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
