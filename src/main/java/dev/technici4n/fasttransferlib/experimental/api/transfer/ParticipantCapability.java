package dev.technici4n.fasttransferlib.experimental.api.transfer;

public interface ParticipantCapability {
    boolean supportsInsertionOf(Class<?> category);

    boolean supportExtractionOf(Class<?> category);
}
