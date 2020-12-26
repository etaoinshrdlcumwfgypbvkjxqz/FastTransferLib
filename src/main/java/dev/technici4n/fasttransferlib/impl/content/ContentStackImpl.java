package dev.technici4n.fasttransferlib.impl.content;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.ContentStack;

public final class ContentStackImpl
        implements ContentStack {
    private final Content content;
    private final long quantity;

    private ContentStackImpl(Content content, long quantity) {
        this.content = content;
        this.quantity = quantity;
    }

    public static ContentStackImpl of(Content content, long quantity) {
        return new ContentStackImpl(content, quantity);
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
