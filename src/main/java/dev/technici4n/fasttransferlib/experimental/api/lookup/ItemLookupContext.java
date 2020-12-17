package dev.technici4n.fasttransferlib.experimental.api.lookup;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import net.minecraft.nbt.CompoundTag;

public interface ItemLookupContext {
    long getAmount();

    CompoundTag getData();

    boolean transform(Context context, long fromCount, Content to, long toCount);
}
