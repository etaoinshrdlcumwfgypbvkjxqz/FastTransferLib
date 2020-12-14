package dev.technici4n.fasttransferlib.experimental.transfer.api.lookup;

import dev.technici4n.fasttransferlib.experimental.transfer.api.Context;
import dev.technici4n.fasttransferlib.experimental.transfer.api.Transferable;
import net.minecraft.nbt.CompoundTag;

public interface ItemLookupContext {
    int getCount();

    CompoundTag getTag();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean set(Context context, Transferable transferable, long count);
}
