package dev.technici4n.fasttransferlib.impl.util;

import java.util.function.Predicate;

public enum FunctionUtilities {
    ;

    public static <T> Predicate<T> getAlwaysTruePredicate() {
        return any -> true;
    }
}
