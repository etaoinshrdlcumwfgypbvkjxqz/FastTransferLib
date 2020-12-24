package dev.technici4n.fasttransferlib.experimental.impl.lookup;

import dev.technici4n.fasttransferlib.experimental.impl.content.ItemContent;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum PlayerItemLookupContext {
    ;

    public static <P extends PlayerEntity> GenericItemStackItemLookupContext ofInventory(P player,
                                                                                         Function<? super P, ? extends ItemStack> targetStackGetter,
                                                                                         BiConsumer<? super P, ? super ItemStack> targetStackSetter) {
        return GenericItemStackItemLookupContext.of(() -> targetStackGetter.apply(player),
                (context, amount) -> {
                    ItemStack targetStack = targetStackGetter.apply(player);
                    if (targetStack.getCount() < amount)
                        return false;
                    int amount1 = Math.toIntExact(amount); // should be within int range
                    context.configure(() -> targetStack.decrement(amount1), () -> targetStack.increment(amount1));
                    return true;
                },
                (context, stack) -> {
                    long amount = stack.getAmount();
                    int amount1 = Math.toIntExact(amount);
                    if (amount == amount1) {
                        ItemStack targetStack = targetStackGetter.apply(player);
                        ItemStack stack1 = ItemContent.asStack(stack.getContent(), amount1);
                        int maxCount = Math.min(stack1.getMaxCount(), player.inventory.getMaxCountPerStack());

                        // check target stack first
                        if (targetStack.isEmpty()) {
                            targetStackSetter.accept(player, stack1.split(maxCount));
                        } else if (stack.getContent().equals(ItemContent.of(targetStack))) {
                            int inserted = maxCount - targetStack.getCount();
                            targetStack.increment(inserted);
                            stack1.decrement(inserted);
                        }

                        // offer to player inventory or drop
                        Int2IntMap insertionMap = new Int2IntOpenHashMap(4);
                        ItemEntity[] droppedEntityReference = {null};
                        context.configure(() -> offerOrDropWithRollbackInformation(player, stack1,
                                insertionMap, droppedEntityReference),
                                () -> rollbackOfferOrDrop(player, insertionMap, droppedEntityReference));
                        return true;
                    }
                    return false;
                });
    }

    public static GenericItemStackItemLookupContext ofHand(PlayerEntity player, Hand hand) {
        return ofInventory(player,
                player1 -> player1.getStackInHand(hand),
                (player1, stack) -> player1.setStackInHand(hand, stack));
    }

    public static GenericItemStackItemLookupContext ofCursor(PlayerEntity player) {
        return ofInventory(player,
                player1 -> player1.inventory.getCursorStack(),
                (player1, stack) -> player1.inventory.setCursorStack(stack));
    }

    private static int getIdealSlot(PlayerInventory instance, ItemStack stack) {
        int index = instance.getOccupiedSlotWithRoomForStack(stack);
        if (index == -1)
            index = instance.getEmptySlot();

        return index;
    }

    private static void offerOrDropWithRollbackInformation(PlayerEntity player, ItemStack stack,
                                                           final Int2IntMap insertionMap, final ItemEntity[] droppedEntityReference) {
        // cursed code here
        PlayerInventory inventory = player.inventory;
        while (!stack.isEmpty()) {
            int slot = getIdealSlot(inventory, stack);
            if (slot == -1) {
                if (!player.getEntityWorld().isClient()) // do it on server only, random involved
                    droppedEntityReference[0] = player.dropItem(stack, false);
                break;
            } else {
                int previousAmount = stack.getCount();
                if (inventory.insertStack(slot, stack)) {
                    int inserted = previousAmount - stack.getCount();
                    insertionMap.put(slot, inserted);
                }
            }
        }
    }

    private static void rollbackOfferOrDrop(PlayerEntity player, Int2IntMap insertionMap, ItemEntity[] droppedEntityReference) {
        PlayerInventory inventory = player.inventory;
        insertionMap.int2IntEntrySet().forEach(insertion ->
                inventory.removeStack(insertion.getIntKey(), insertion.getIntValue()));
        ItemEntity droppedEntity = droppedEntityReference[0];
        if (droppedEntity != null)
            droppedEntity.remove(); // silently removes it, do not use kill
    }
}
