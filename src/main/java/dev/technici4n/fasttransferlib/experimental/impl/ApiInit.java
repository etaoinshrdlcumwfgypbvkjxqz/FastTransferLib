package dev.technici4n.fasttransferlib.experimental.impl;

import dev.technici4n.fasttransferlib.experimental.impl.compat.vanilla.VanillaCompat;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

public enum ApiInit {
    ;

    static {
        // Vanilla
        VanillaCompat.initializeClass();

        // LBA
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_items")) {
            try {
                Class<?> clazz = Class.forName("dev.technici4n.fasttransferlib.experimental.impl.compat.lba.LbaCompat$ItemCompat");
                Method init = clazz.getMethod("initializeClass");
                init.invoke(null);
            } catch (Exception ex) {
                throw new RuntimeException("LBA was detected, but item compat loading failed", ex);
            }
        }
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_fluids")) {
            try {
                Class<?> clazz = Class.forName("dev.technici4n.fasttransferlib.experimental.impl.compat.lba.LbaCompat$FluidCompat");
                Method init = clazz.getMethod("initializeClass");
                init.invoke(null);
            } catch (Exception ex) {
                throw new RuntimeException("LBA was detected, but fluid compat loading failed", ex);
            }
        }

        // TR
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            try {
                Class<?> clazz = Class.forName("dev.technici4n.fasttransferlib.experimental.impl.compat.tr.TrCompat$EnergyCompat");
                Method init = clazz.getMethod("initializeClass");
                init.invoke(null);
            } catch (Exception ex) {
                throw new RuntimeException("TR energy was detected, but energy compat loading failed", ex);
            }
        }
    }

    public static void initializeClass() {}
}
