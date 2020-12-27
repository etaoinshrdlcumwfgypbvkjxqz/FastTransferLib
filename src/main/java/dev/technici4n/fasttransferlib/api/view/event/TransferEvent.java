package dev.technici4n.fasttransferlib.api.view.event;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;

public interface TransferEvent
        extends PushEvent, PullEvent {
    TransferAction getType();

    Content getContent();

    long getQuantity();
}
