package dev.technici4n.fasttransferlib.api.lookup;

import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import net.minecraft.nbt.CompoundTag;

public interface ItemLookupContext {
    long getAmount();

    CompoundTag getData();

    boolean transform(Context context, long fromCount, Content to, long toCount);

    static boolean isInvalid(ItemLookupContext instance) {
        return instance.getAmount() == 0L;
    }
}
