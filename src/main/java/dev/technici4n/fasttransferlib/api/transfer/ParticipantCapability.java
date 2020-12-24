package dev.technici4n.fasttransferlib.api.transfer;

public interface ParticipantCapability {
    boolean supportsInsertionOf(Class<?> category);

    boolean supportExtractionOf(Class<?> category);
}
