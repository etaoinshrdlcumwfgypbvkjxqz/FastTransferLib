package dev.technici4n.fasttransferlib.experimental.api.view;

import dev.technici4n.fasttransferlib.experimental.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.item.ItemApiLookup;
import dev.technici4n.fasttransferlib.experimental.api.lookup.item.ItemApiLookupRegistry;
import dev.technici4n.fasttransferlib.experimental.impl.ApiInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookupRegistry;
import net.minecraft.util.Identifier;

public enum ViewApi {
    ;

    public static final BlockApiLookup<View, BlockLookupContext> BLOCK =
            BlockApiLookupRegistry.getLookup(new Identifier("fasttransferlib:block_view"), View.class, BlockLookupContext.class);
    public static final ItemApiLookup<View, ItemLookupContext> ITEM =
            ItemApiLookupRegistry.getLookup(new Identifier("fasttransferlib:item_view"), View.class, ItemLookupContext.class);

    static {
        ApiInit.initializeClass();
    }
}
