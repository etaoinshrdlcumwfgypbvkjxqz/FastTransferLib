package dev.technici4n.fasttransferlib.experimental.api.transfer;

import dev.technici4n.fasttransferlib.experimental.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.api.lookup.item.ItemApiLookup;
import dev.technici4n.fasttransferlib.experimental.api.lookup.item.ItemApiLookupRegistry;
import dev.technici4n.fasttransferlib.experimental.impl.ApiInit;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookupRegistry;
import net.minecraft.util.Identifier;

public enum TransferApi {
    ;

    public static final BlockApiLookup<Participant, BlockLookupContext> BLOCK =
            BlockApiLookupRegistry.getLookup(new Identifier("fasttransferlib:block_participant"), Participant.class, BlockLookupContext.class);
    public static final ItemApiLookup<Participant, ItemLookupContext> ITEM =
            ItemApiLookupRegistry.getLookup(new Identifier("fasttransferlib:item_participant"), Participant.class, ItemLookupContext.class);

    static {
        ApiInit.initializeClass();
    }
}
