package dev.technici4n.fasttransferlib.api.lookup;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import net.minecraft.nbt.CompoundTag;

public interface ItemLookupContext {
    long getQuantity();

    CompoundTag getData();

    boolean transform(Context context, long fromCount, Content to, long toCount);

    static boolean isInvalid(ItemLookupContext instance) {
        return instance.getQuantity() == 0L;
    }
}
