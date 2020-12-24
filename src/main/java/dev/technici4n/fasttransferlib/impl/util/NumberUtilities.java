package dev.technici4n.fasttransferlib.impl.util;

public enum NumberUtilities {
    ;

    public static long toSaturatedInteger(double value) {
        // does not seem good enough
        if (Double.isFinite(value)) {
            if (value > Long.MAX_VALUE)
                return Long.MAX_VALUE;
            else if (value < Long.MIN_VALUE)
                return Long.MIN_VALUE;
        }
        return (long) value;
    }
}
