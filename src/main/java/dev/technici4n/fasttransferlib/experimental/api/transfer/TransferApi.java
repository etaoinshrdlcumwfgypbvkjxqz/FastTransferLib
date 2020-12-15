package dev.technici4n.fasttransferlib.experimental.api.transfer;

import dev.technici4n.fasttransferlib.experimental.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.item.ItemApiLookup;
import dev.technici4n.fasttransferlib.experimental.api.lookup.item.ItemApiLookupRegistry;
import dev.technici4n.fasttransferlib.experimental.impl.compat.VanillaCompat;
import net.fabricmc.fabric.api.provider.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.provider.v1.block.BlockApiLookupRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

public enum TransferApi {
    ;

    public static final BlockApiLookup<Participant, BlockLookupContext> BLOCK =
            BlockApiLookupRegistry.getLookup(new Identifier("fasttransferlib:block_participant"), Participant.class, BlockLookupContext.class);
    public static final ItemApiLookup<Participant, ItemLookupContext> ITEM =
            ItemApiLookupRegistry.getLookup(new Identifier("fasttransferlib:item_participant"), Participant.class, ItemLookupContext.class);

    static {
        VanillaCompat.initializeClass();
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_items")) {
            try {
                Class<?> clazz = Class.forName("dev.technici4n.fasttransferlib.experimental.impl.compat.LbaCompat.ItemCompat");
                Method init = clazz.getMethod("initializeClass");
                init.invoke(null);
            } catch (Exception ex) {
                throw new RuntimeException("LBA was detected, but item compat loading failed", ex);
            }
        }
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_fluids")) {
            try {
                Class<?> clazz = Class.forName("dev.technici4n.fasttransferlib.experimental.impl.compat.LbaCompat.FluidCompat");
                Method init = clazz.getMethod("initializeClass");
                init.invoke(null);
            } catch (Exception ex) {
                throw new RuntimeException("LBA was detected, but fluid compat loading failed", ex);
            }
        }
    }
}
