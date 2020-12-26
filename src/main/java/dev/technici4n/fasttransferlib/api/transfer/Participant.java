package dev.technici4n.fasttransferlib.api.transfer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Queryable;

public interface Participant
        extends Queryable {
    long insert(Context context, Content content, long maxQuantity);

    long extract(Context context, Content content, long maxQuantity);
}
