package dev.technici4n.fasttransferlib.impl.query;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.query.TransferContentQuery;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;

public class TransferContentQueryImpl
        extends TransferCategoryQueryImpl
        implements TransferContentQuery {
    private final Content content;

    protected TransferContentQueryImpl(TransferAction action, Content content) {
        super(action, content.getCategory());
        this.content = content;
    }

    public static TransferContentQueryImpl of(TransferAction action, Content content) {
        return new TransferContentQueryImpl(action, content);
    }

    @Override
    public Content getContent() {
        return content;
    }
}
