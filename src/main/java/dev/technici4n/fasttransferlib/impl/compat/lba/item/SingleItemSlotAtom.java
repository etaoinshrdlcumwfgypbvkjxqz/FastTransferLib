package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.SingleItemSlotView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatImplUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import net.minecraft.item.Item;

import java.util.OptionalLong;

public class SingleItemSlotAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final SingleItemSlotView delegate;

    protected SingleItemSlotAtom(SingleItemSlotView delegate) {
        super(Item.class);
        this.delegate = delegate;
    }

    public static SingleItemSlotAtom of(SingleItemSlotView delegate) {
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
    public OptionalLong getCapacity() {
        return OptionalLong.of(getDelegate().getMaxAmount(getDelegate().get()));
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        return LbaCompatImplUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        return LbaCompatImplUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
    }

    protected SingleItemSlotView getDelegate() {
        return delegate;
    }
}
