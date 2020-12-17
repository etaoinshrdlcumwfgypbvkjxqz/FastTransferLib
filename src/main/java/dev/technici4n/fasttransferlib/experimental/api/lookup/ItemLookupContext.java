package dev.technici4n.fasttransferlib.experimental.api.lookup;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import net.minecraft.nbt.CompoundTag;

// TODO set is not a good solution
public interface ItemLookupContext {
    long getCount();

    CompoundTag getTag();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean set(Context context, Content content, long count);
}
