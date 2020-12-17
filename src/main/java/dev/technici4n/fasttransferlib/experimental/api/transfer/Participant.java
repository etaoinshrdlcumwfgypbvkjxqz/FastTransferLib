package dev.technici4n.fasttransferlib.experimental.api.transfer;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;

public interface Participant {
    long insert(Context context, Content content, long maxAmount);

    long extract(Context context, Content content, long maxAmount);
}
