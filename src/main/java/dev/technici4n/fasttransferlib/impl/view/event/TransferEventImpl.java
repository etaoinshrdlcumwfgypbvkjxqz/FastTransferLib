package dev.technici4n.fasttransferlib.impl.view.event;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;

public final class TransferEventImpl
        implements TransferEvent {
    private final TransferAction action;
    private final Content content;
    private final long quantity;

    private TransferEventImpl(TransferAction action, Content content, long quantity) {
        this.action = action;
        this.content = content;
        this.quantity = quantity;
    }

    public static TransferEventImpl of(TransferAction action, Content content, long quantity) {
        return new TransferEventImpl(action, content, quantity);
    }

    public static TransferEventImpl ofInsertion(Content content, long quantity) {
        return of(TransferAction.INSERT, content, quantity);
    }

    public static TransferEventImpl ofExtraction(Content content, long quantity) {
        return of(TransferAction.EXTRACT, content, quantity);
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
    public long getQuantity() {
        return quantity;
    }
}
