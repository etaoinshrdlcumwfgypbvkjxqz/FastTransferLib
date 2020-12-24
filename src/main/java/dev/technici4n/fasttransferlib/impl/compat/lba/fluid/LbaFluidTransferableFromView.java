package dev.technici4n.fasttransferlib.impl.compat.lba.fluid;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.ItemTransferable;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import dev.technici4n.fasttransferlib.api.Context;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.impl.compat.lba.LbaCompatUtil;
import dev.technici4n.fasttransferlib.impl.content.ItemContent;
import dev.technici4n.fasttransferlib.impl.util.ViewUtilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LbaFluidTransferableFromView
        implements ItemTransferable {
    private final View delegate;

    protected LbaFluidTransferableFromView(View delegate) {
        // the use of 'FluidFilter' means a view is required
        this.delegate = delegate;
    }

    public static LbaFluidTransferableFromView of(View delegate) {
        return new LbaFluidTransferableFromView(delegate);
    }

    protected View getDelegate() {
        return delegate;
    }

    @Override
    public ItemStack attemptExtraction(ItemFilter itemFilter, int maxAmount, Simulation simulation) {
        Context context = LbaCompatUtil.asContext(simulation);

        Content[] contentLock = {null};
        long extracted = ViewUtilities.extract(getDelegate(),
                context,
                maxAmount,
                atom -> {
                    Content content = atom.getContent();
                    if (content.isEmpty() || content.getCategory() != Item.class)
                        return null;
                    Content contentLock1 = contentLock[0];
                    if (contentLock1 == null && itemFilter.matches(ItemContent.asStack(content, 1))) {
                        return contentLock[0] = content;
                    }
                    return contentLock1;
                });
        int extracted1 = Math.toIntExact(extracted); // within int range

        Content contentLock1 = contentLock[0];
        if (contentLock1 == null)
            return ItemStack.EMPTY;
        assert contentLock1.getCategory() == Item.class;
        return ItemContent.asStack(contentLock1, extracted1);
    }

    @Override
    public ItemStack attemptInsertion(ItemStack itemStack, Simulation simulation) {
        Context context = LbaCompatUtil.asContext(simulation);

        Content content = ItemContent.of(itemStack);
        long leftover = ViewUtilities.insert(getDelegate(),
                context,
                content,
                itemStack.getCount());
        int leftover1 = Math.toIntExact(leftover); // within int range

        return ItemContent.asStack(content, leftover1);
    }
}
