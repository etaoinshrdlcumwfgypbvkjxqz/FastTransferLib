package dev.technici4n.fasttransferlib.impl.base.transfer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.transfer.Participant;

public abstract class AbstractParticipant
        implements Participant {
    @Override
    public final long insert(Context context, Content content, long maxAmount) {
        if (content.isEmpty())
            return maxAmount;

        return insertNonEmpty(context, content, maxAmount);
    }

    @Override
    public final long extract(Context context, Content content, long maxAmount) {
        if (content.isEmpty())
            return 0L;

        return extractNonEmpty(context, content, maxAmount);
    }

    protected abstract long insertNonEmpty(Context context, Content content, long maxAmount);

    protected abstract long extractNonEmpty(Context context, Content content, long maxAmount);
}
