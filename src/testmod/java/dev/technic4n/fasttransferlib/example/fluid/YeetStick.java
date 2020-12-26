package dev.technic4n.fasttransferlib.example.fluid;

import com.google.common.collect.Iterators;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.FluidConstants;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.ViewApi;
import dev.technici4n.fasttransferlib.impl.context.ExecutionContext;
import dev.technici4n.fasttransferlib.impl.lookup.BlockLookupContextImpl;
import dev.technici4n.fasttransferlib.impl.util.FluidTextUtilities;
import net.minecraft.entity.player.PlayerEntity;
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
				Atom atom = Iterators.getNext(view.iterator(), null);
				if (atom != null) {
					Content content = atom.getContent();
					long extracted = atom.extract(ExecutionContext.getInstance(), content, 2 * FluidConstants.BOTTLE);
					if (extracted > 0L) {
						PlayerEntity player = context.getPlayer();
						if (player != null) {
							player.sendMessage(
									new LiteralText(String.format("Extracted %s millibuckets of %s",
											FluidTextUtilities.getUnicodeMillibuckets(extracted, true), content)),
									false);
							player.sendMessage(
									new LiteralText(String.format("Extracted %s millibuckets of %s",
											FluidTextUtilities.getUnicodeMillibuckets(extracted, false), content)),
									false);
						}
						return ActionResult.SUCCESS;
					}
				}
			}
		}

		return ActionResult.PASS;
	}
}
