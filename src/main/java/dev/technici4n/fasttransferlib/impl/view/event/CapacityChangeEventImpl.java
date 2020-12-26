package dev.technici4n.fasttransferlib.impl.view.event;

import dev.technici4n.fasttransferlib.api.view.event.CapacityChangeEvent;

import java.util.OptionalLong;

public class CapacityChangeEventImpl
        implements CapacityChangeEvent {
    private final boolean hasPreviousCapacity;
    private final long previousCapacity0;
    private final boolean hasCurrentCapacity;
    private final long currentCapacity0;

    protected CapacityChangeEventImpl(boolean hasPreviousCapacity, long previousCapacity0, boolean hasCurrentCapacity, long currentCapacity0) {
        this.hasPreviousCapacity = hasPreviousCapacity;
        this.previousCapacity0 = previousCapacity0;
        this.hasCurrentCapacity = hasCurrentCapacity;
        this.currentCapacity0 = currentCapacity0;
    }

    public static CapacityChangeEventImpl of(long previousCapacity, long currentCapacity) {
        return new CapacityChangeEventImpl(true, previousCapacity, true, currentCapacity);
    }

    public static CapacityChangeEventImpl ofCurrent(long currentCapacity) {
        return new CapacityChangeEventImpl(false, 0L, true, currentCapacity);
    }

    public static CapacityChangeEventImpl ofPrevious(long previousCapacity) {
        return new CapacityChangeEventImpl(true, previousCapacity, false, 0L);
    }

    @Override
    public OptionalLong getPreviousCapacity() {
        return isHasPreviousCapacity() ? OptionalLong.of(getPreviousCapacity0()) : OptionalLong.empty();
    }

    @Override
    public OptionalLong getCurrentCapacity() {
        return isHasCurrentCapacity() ? OptionalLong.of(getCurrentCapacity0()) : OptionalLong.empty();
    }

    protected boolean isHasPreviousCapacity() {
        return hasPreviousCapacity;
    }

    protected long getPreviousCapacity0() {
        return previousCapacity0;
    }

    protected boolean isHasCurrentCapacity() {
        return hasCurrentCapacity;
    }

    protected long getCurrentCapacity0() {
        return currentCapacity0;
    }
}
