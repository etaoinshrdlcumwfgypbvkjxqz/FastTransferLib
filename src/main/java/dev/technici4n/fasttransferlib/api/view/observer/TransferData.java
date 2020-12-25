package dev.technici4n.fasttransferlib.api.view.observer;

import dev.technici4n.fasttransferlib.api.content.Content;

public interface TransferData {
    Type getType();

    Content getContent();

    long getAmount();

    enum Type {
        INSERT,
        EXTRACT,
        ;

        public static Type fromDifference(boolean positive) {
            return positive ? INSERT : EXTRACT;
        }
    }
}
