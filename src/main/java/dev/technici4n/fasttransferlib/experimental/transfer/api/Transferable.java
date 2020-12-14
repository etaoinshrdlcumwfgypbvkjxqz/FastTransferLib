package dev.technici4n.fasttransferlib.experimental.transfer.api;

public interface Transferable {
    Object getType();

    Object getData();

    Class<?> getCategory();
}
