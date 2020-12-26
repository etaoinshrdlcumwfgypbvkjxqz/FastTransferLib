package dev.technici4n.fasttransferlib.api.view.event;

import java.util.OptionalLong;

public interface CapacityChangeEvent {
    OptionalLong getPreviousCapacity();

    OptionalLong getCurrentCapacity();
}
