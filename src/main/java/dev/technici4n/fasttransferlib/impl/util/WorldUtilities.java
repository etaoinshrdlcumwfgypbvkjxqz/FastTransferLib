package dev.technici4n.fasttransferlib.impl.util;

public enum WorldUtilities {
    ;

    public static final int PROPAGATE_CHANGE = 1;
    public static final int NOTIFY_LISTENERS = 1 << 1;
    public static final int NO_REDRAW = 1 << 2;
    public static final int REDRAW_ON_MAIN_THREAD = 1 << 3;
    public static final int FORCE_STATE = 1 << 4;
    public static final int SKIP_DROPS = 1 << 5;
    public static final int MOVED = 1 << 6;
    public static final int DEFAULT_FLAGS = PROPAGATE_CHANGE | NOTIFY_LISTENERS;
}
