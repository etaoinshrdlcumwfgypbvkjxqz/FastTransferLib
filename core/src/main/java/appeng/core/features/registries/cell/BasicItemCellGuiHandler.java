
package appeng.core.features.registries.cell;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;

public class BasicItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public <T extends IAEStack<T>> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public void openChestGui(final PlayerEntity player, final IChestOrDrive chest, final ICellHandler cellHandler,
            final IMEInventoryHandler inv, final ItemStack is, final IStorageChannel chan) {
   // FIXME FABRIC     ContainerOpener.openContainer(MEMonitorableContainer.TYPE, player,
   // FIXME FABRIC             ContainerLocator.forTileEntitySide((BlockEntity) chest, chest.getUp()));
    }
}