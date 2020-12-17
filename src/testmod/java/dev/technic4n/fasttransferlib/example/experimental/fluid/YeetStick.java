package dev.technic4n.fasttransferlib.example.experimental.fluid;

import com.google.common.collect.Iterators;
import dev.technici4n.fasttransferlib.api.fluid.FluidConstants;
import dev.technici4n.fasttransferlib.api.fluid.FluidTextHelper;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.api.view.ViewApi;
import dev.technici4n.fasttransferlib.experimental.impl.lookup.BlockLookupContextImpl;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.context.ExecutionContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;

public class YeetStick extends Item {
	public YeetStick(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (!context.getWorld().isClient()) {
			View view = ViewApi.BLOCK.get(context.getWorld(), context.getBlockPos(), BlockLookupContextImpl.of(context.getSide()));
			if (view != null && view.estimateAtomSize() >= 1L) {
				Atom atom = Iterators.getNext(view.getAtomIterator(), null);
				if (atom != null) {
					Content content = atom.getContent();
					long extracted = atom.extract(ExecutionContext.getInstance(), content, 2 * FluidConstants.BOTTLE);
					if (extracted > 0L) {
						context.getPlayer().sendMessage(
								new LiteralText(String.format("Extracted %s millibuckets of %s",
										FluidTextHelper.getUnicodeMillibuckets(extracted, true), content)),
								false);
						context.getPlayer().sendMessage(
								new LiteralText(String.format("Extracted %s millibuckets of %s",
										FluidTextHelper.getUnicodeMillibuckets(extracted, false), content)),
								false);

						return ActionResult.SUCCESS;
					}
				}
			}
		}

		return ActionResult.PASS;
	}
}
