package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.SingleItemSlotView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SingleItemSlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final SingleItemSlot delegate;

    protected SingleItemSlotAtom(SingleItemSlot delegate) {
        super(Item.class);
        this.delegate = delegate;
    }

    public static SingleItemSlotAtom of(SingleItemSlot delegate) {
        return new SingleItemSlotAtom(delegate);
    }

    @Override
    public Content getContent() {
        return ItemContent.of(getDelegate().get());
    }

    @Override
    public long getAmount() {
        return getDelegate().get().getCount();
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        return insertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        return extractImpl(getDelegate(), context, content, maxAmount);
    }

    protected SingleItemSlot getDelegate() {
        return delegate;
    }

    public static long insertImpl(ItemTransferable transferable, Context context, Content itemContent, long maxAmount) {
        /* note
        This assumes that an insertion can always be reverted by an extraction that follows the insertion,
        which may be reasonable for most cases.
        However, if this is violated, things may go wrong.
         */
        int amount1 = Ints.saturatedCast(maxAmount);

        ItemStack leftover = transferable.attemptInsertion(ItemContent.asStack(itemContent, amount1), Simulation.SIMULATE);
        int leftoverAmount = leftover.getCount();

        if (amount1 != leftoverAmount) {
            int insert = amount1 - leftoverAmount;
            context.configure(() -> transferable.insert(ItemContent.asStack(itemContent, insert)),
                    () -> transferable.extract(ItemContent.asStack(itemContent, insert), insert));
            return maxAmount - insert;
        }
        return maxAmount;
    }

    public static long extractImpl(ItemTransferable transferable, Context context, Content itemContent, long maxAmount) {
        /* note
        This assumes that an extraction can always be reverted by an insertion that follows the extraction,
        which is reasonable for a tank.
        However, if this is wrong, things may go wrong.
         */
        int amount1 = Ints.saturatedCast(maxAmount);

        ItemStack extracted = transferable.attemptExtraction(new ExactItemStackFilter(ItemContent.asStack(itemContent, amount1)), amount1, Simulation.SIMULATE);
        int extractedAmount = extracted.getCount();

        if (extractedAmount > 0) {
            context.configure(() -> transferable.extract(new ExactItemStackFilter(ItemContent.asStack(itemContent, amount1)), extractedAmount),
                    () -> transferable.insert(ItemContent.asStack(itemContent, amount1)));
            return extractedAmount;
        }
        return 0L;
    }
}
