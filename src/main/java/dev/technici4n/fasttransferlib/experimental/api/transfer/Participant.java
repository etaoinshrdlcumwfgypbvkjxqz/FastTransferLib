package dev.technici4n.fasttransferlib.experimental.api.transfer;

import dev.technici4n.fasttransferlib.experimental.api.Instance;

public interface Participant {
    long insert(Context context, Instance instance, long maxAmount);

    long extract(Context context, Instance instance, long maxAmount);
}
