package dev.technici4n.fasttransferlib.api.view.event;

import java.util.OptionalLong;

public interface CapacityChangeEvent
        extends PushEvent, PullEvent {
    OptionalLong getPreviousCapacity();

    OptionalLong getCurrentCapacity();
}
