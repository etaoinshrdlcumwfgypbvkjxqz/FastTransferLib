package dev.technici4n.fasttransferlib.api.transfer;

import java.util.function.LongUnaryOperator;

public enum TransferAction
        implements LongUnaryOperator {
    INSERT {
        @Override
        public long applyAsLong(long value) {
            return value;
        }
    },
    EXTRACT {
        @Override
        public long applyAsLong(long value) {
            return -value;
        }
    },
    ;

    public static TransferAction fromDifference(boolean positive) {
        return positive ? INSERT : EXTRACT;
    }
}
