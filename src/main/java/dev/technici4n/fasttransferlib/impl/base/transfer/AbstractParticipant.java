package dev.technici4n.fasttransferlib.impl.base.transfer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.transfer.Participant;

public abstract class AbstractParticipant
        implements Participant {
    @Override
    public final long insert(Context context, Content content, long maxQuantity) {
        if (content.isEmpty())
            return maxQuantity;

        return insertNonEmpty(context, content, maxQuantity);
    }

    @Override
    public final long extract(Context context, Content content, long maxQuantity) {
        if (content.isEmpty())
            return 0L;

        return extractNonEmpty(context, content, maxQuantity);
    }

    protected abstract long insertNonEmpty(Context context, Content content, long maxQuantity);

    protected abstract long extractNonEmpty(Context context, Content content, long maxQuantity);
}
