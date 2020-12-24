package dev.technici4n.fasttransferlib.api.transfer;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;

public interface Participant {
    long insert(Context context, Content content, long maxAmount);

    long extract(Context context, Content content, long maxAmount);
}
