package dev.technici4n.fasttransferlib.experimental.api.lookup;

import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import net.minecraft.nbt.CompoundTag;

public interface ItemLookupContext {
    long getAmount();

    CompoundTag getData();

    boolean transform(Context context, long fromCount, Content to, long toCount);

    static boolean isInvalid(ItemLookupContext instance) {
        return instance.getAmount() == 0L;
    }
}
