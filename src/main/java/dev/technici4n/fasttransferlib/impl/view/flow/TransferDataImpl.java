package dev.technici4n.fasttransferlib.impl.view.flow;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;

public final class TransferDataImpl
        implements TransferData {
    private final TransferAction action;
    private final Content content;
    private final long amount;

    private TransferDataImpl(TransferAction action, Content content, long amount) {
        this.action = action;
        this.content = content;
        this.amount = amount;
    }

    public static TransferDataImpl of(TransferAction action, Content content, long amount) {
        return new TransferDataImpl(action, content, amount);
    }

    public static TransferDataImpl ofInsertion(Content content, long amount) {
        return of(TransferAction.INSERT, content, amount);
    }

    public static TransferDataImpl ofExtraction(Content content, long amount) {
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
