package dev.technici4n.fasttransferlib.experimental.transfer.api;

public interface ParticipantCapability {
    boolean supportsInsertionOf(Class<?> category);

    boolean supportExtractionOf(Class<?> category);
}
