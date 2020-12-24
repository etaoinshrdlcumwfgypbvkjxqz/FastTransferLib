package dev.technici4n.fasttransferlib.impl.content;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.ContentStack;

public final class ContentStackImpl
        implements ContentStack {
    private final Content content;
    private final long amount;

    private ContentStackImpl(Content content, long amount) {
        this.content = content;
        this.amount = amount;
    }

    public static ContentStackImpl of(Content content, long amount) {
        return new ContentStackImpl(content, amount);
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