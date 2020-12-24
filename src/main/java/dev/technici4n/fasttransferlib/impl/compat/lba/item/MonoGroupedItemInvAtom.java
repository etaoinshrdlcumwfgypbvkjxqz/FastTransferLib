package dev.technici4n.fasttransferlib.impl.compat.lba.item;

import alexiil.mc.lib.attributes.item.GroupedItemInvView;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatImplUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.OptionalLong;

public class MonoGroupedItemInvAtom
        extends AbstractMonoCategoryAtom<Item> {
    private final GroupedItemInvView delegate;
    private final Content content;
    private final ItemStack key;

    protected MonoGroupedItemInvAtom(GroupedItemInvView delegate, Content content) {
        super(Item.class);
        assert content.getCategory() == Item.class;

        this.delegate = delegate;
        this.content = content;
        this.key = ItemContent.asStack(this.content, 1);
    }

    public static MonoGroupedItemInvAtom of(GroupedItemInvView delegate, Content content) {
        return new MonoGroupedItemInvAtom(delegate, content);
    }

    protected GroupedItemInvView getDelegate() {
        return delegate;
    }

    protected ItemStack getKey() {
        return key;
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public long getAmount() {
        return getDelegate().getAmount(getKey());
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(getDelegate().getCapacity(getKey()));
    }

    @Override
    protected long insert(Context context, Content content, Item type, long maxAmount) {
        if (content.equals(getContent()))
            return LbaCompatImplUtil.genericInsertImpl(getDelegate(), context, content, maxAmount);
        return maxAmount;
    }

    @Override
    protected long extract(Context context, Content content, Item type, long maxAmount) {
        if (content.equals(getContent()))
            return LbaCompatImplUtil.genericExtractImpl(getDelegate(), context, content, maxAmount);
        return 0L;
    }
}
