package dev.technici4n.fasttransferlib.experimental.transfer.api;

public interface Participant {
    long insert(Context context, Transferable transferable, long maxAmount);

    long extract(Context context, Transferable transferable, long maxAmount);
}
