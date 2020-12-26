package dev.technici4n.fasttransferlib.api.view.flow;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;

public interface TransferData {
    TransferAction getType();

    Content getContent();

    long getAmount();
}
