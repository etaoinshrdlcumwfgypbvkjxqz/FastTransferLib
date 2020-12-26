package dev.technici4n.fasttransferlib.api.query;

import dev.technici4n.fasttransferlib.api.transfer.TransferAction;

public interface TransferQuery
        extends Query {
    TransferAction getAction();
}
