package dev.technici4n.fasttransferlib.experimental.api.lookup;

import dev.technici4n.fasttransferlib.experimental.api.Instance;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Context;
import net.minecraft.nbt.CompoundTag;

public interface ItemLookupContext {
    int getCount();

    CompoundTag getTag();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean set(Context context, Instance instance, long count);
}
