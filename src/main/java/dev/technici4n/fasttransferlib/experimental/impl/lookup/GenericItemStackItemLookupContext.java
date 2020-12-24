package dev.technici4n.fasttransferlib.experimental.impl.lookup;

import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import dev.technici4n.fasttransferlib.experimental.api.content.ContentStack;
import dev.technici4n.fasttransferlib.experimental.api.lookup.ItemLookupContext;
import dev.technici4n.fasttransferlib.experimental.impl.content.ContentStackImpl;
import dev.technici4n.fasttransferlib.experimental.impl.context.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class GenericItemStackItemLookupContext
        implements ItemLookupContext {
    private final Supplier<? extends ItemStack> stackGetter;
    private final BiPredicate<? super Context, ? super Long> stackConsumer;
    // ideally a Function3 would work, but it is not provided by Java by default
    private final BiPredicate<? super Context, ? super ContentStack> stackAdder;
    private boolean replaced = false;

    protected GenericItemStackItemLookupContext(Supplier<? extends ItemStack> stackGetter,
                                                BiPredicate<? super Context, ? super Long> stackConsumer,
                                                BiPredicate<? super Context, ? super ContentStack> stackAdder) {
        this.stackGetter = stackGetter;
        this.stackConsumer = stackConsumer;
        this.stackAdder = stackAdder;
    }

    public static GenericItemStackItemLookupContext of(Supplier<? extends ItemStack> stackGetter,
                                                       BiPredicate<? super Context, ? super Long> stackConsumer,
                                                       BiPredicate<? super Context, ? super ContentStack> stackAdder) {
        return new GenericItemStackItemLookupContext(stackGetter, stackConsumer, stackAdder);
    }

    @Override
    public long getAmount() {
        return isReplaced() ? 0L : getStackGetter().get().getCount();
    }

    @Override
    public CompoundTag getData() {
        @Nullable CompoundTag tag = isReplaced() ? null : getStackGetter().get().getTag();
        return tag == null ? new CompoundTag() : tag.copy();
    }

    @Override
    public boolean transform(Context context, long fromCount, Content to, long toCount) {
        assert fromCount >= 0L;
        assert toCount >= 0L;
        // assume fromCount and toCount are non-negative from here

        if (fromCount == 0L) {
            /*
            Do nothing with original stack.
            not replaced -> add stack
            replaced -> do not use setter, add stack
            So just add stack.
             */
            if (to.isEmpty() || toCount == 0L)
                return true; // transform nothing to nothing, always succeed
            return getStackAdder().test(context, ContentStackImpl.of(to, toCount)); // one atomic operation for us
        } else {
            long amount = getAmount();
            if (amount >= fromCount) {
                // has enough and not replaced (0L case handled above)
                assert !isReplaced();

                try (TransactionContext consumeAndAddTransaction = new TransactionContext(3)) {
                    // subtract
                    if (getStackConsumer().test(consumeAndAddTransaction, fromCount)) {
                        if (amount == fromCount) {
                            // is a replace operation
                            consumeAndAddTransaction.configure(() -> setReplaced(true), () -> setReplaced(false));
                        }
                        if (to.isEmpty() || toCount == 0L || getStackAdder().test(consumeAndAddTransaction, ContentStackImpl.of(to, toCount))) {
                            consumeAndAddTransaction.commitWith(context); // commit using context
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected Supplier<? extends ItemStack> getStackGetter() {
        return stackGetter;
    }

    protected BiPredicate<? super Context, ? super Long> getStackConsumer() {
        return stackConsumer;
    }

    protected BiPredicate<? super Context, ? super ContentStack> getStackAdder() {
        return stackAdder;
    }

    protected boolean isReplaced() {
        return replaced;
    }

    protected void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }
}
