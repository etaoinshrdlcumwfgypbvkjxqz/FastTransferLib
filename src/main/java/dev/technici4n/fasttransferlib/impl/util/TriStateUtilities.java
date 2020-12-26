package dev.technici4n.fasttransferlib.impl.util;

import net.fabricmc.fabric.api.util.TriState;

import java.util.function.Supplier;

public enum TriStateUtilities {
    ;

    public static TriState orGet(TriState instance, Supplier<? extends TriState> or) {
        return instance == TriState.DEFAULT ? or.get() : instance;
    }

    public static TriState or(TriState instance, TriState or) {
        return instance == TriState.DEFAULT ? or : instance;
    }
}
