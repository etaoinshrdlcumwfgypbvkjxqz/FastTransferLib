/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.container.slot;


import appeng.core.Api;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.util.Platform;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class SlotRestrictedInput extends AppEngSlot
{

	private final PlacableItemType which;
	private final PlayerInventory p;
	private boolean allowEdit = true;
	private int stackLimit = -1;

	public SlotRestrictedInput( final PlacableItemType valid, final IItemHandler i, final int slotIndex, final int x, final int y, final PlayerInventory p )
	{
		super( i, slotIndex, x, y );
		this.which = valid;
		this.setIIcon( valid.IIcon );
		this.p = p;
	}

	@Override
	public int getSlotStackLimit()
	{
		if( this.stackLimit != -1 )
		{
			return this.stackLimit;
		}
		return super.getSlotStackLimit();
	}

	public boolean isValid( final ItemStack is, final World theWorld )
	{
		if( this.which == PlacableItemType.VALID_ENCODED_PATTERN_W_OUTPUT )
		{
			final ICraftingPatternDetails ap = is.getItem() instanceof ICraftingPatternItem ? ( (ICraftingPatternItem) is.getItem() ).getPatternForItem( is,
					theWorld ) : null;
			return ap != null;
		}
		return true;
	}

	public Slot setStackLimit(final int i )
	{
		this.stackLimit = i;
		return this;
	}

	@Override
	public boolean isItemValid( final ItemStack i )
	{
		if( !this.getContainer().isValidForSlot( this, i ) )
		{
			return false;
		}

		if( i.isEmpty() )
		{
			return false;
		}

		if( i.getItem() == Items.AIR )
		{
			return false;
		}

		if( !super.isItemValid( i ) )
		{
			return false;
		}

		if( !this.isAllowEdit() )
		{
			return false;
		}

		final IDefinitions definitions = Api.INSTANCE.definitions();
		final IMaterials materials = definitions.materials();
		final IItems items = definitions.items();

		switch( this.which )
		{
			case ENCODED_CRAFTING_PATTERN:
				if( i.getItem() instanceof ICraftingPatternItem )
				{
					final ICraftingPatternItem b = (ICraftingPatternItem) i.getItem();
					final ICraftingPatternDetails de = b.getPatternForItem( i, this.p.player.world );
					if( de != null )
					{
						return de.isCraftable();
					}
				}
				return false;
			case VALID_ENCODED_PATTERN_W_OUTPUT:
			case ENCODED_PATTERN_W_OUTPUT:
			case ENCODED_PATTERN:
			{
				if( i.getItem() instanceof ICraftingPatternItem )
				{
					return true;
				}
				// ICraftingPatternDetails pattern = i.getItem() instanceof ICraftingPatternItem ?
				// ((ICraftingPatternItem)
				// i.getItem()).getPatternForItem( i ) : null;
				return false;// pattern != null;
			}
			case BLANK_PATTERN:
				return materials.blankPattern().isSameAs( i );

			case PATTERN:

				if( i.getItem() instanceof ICraftingPatternItem )
				{
					return true;
				}

				return materials.blankPattern().isSameAs( i );

			case INSCRIBER_PLATE:
				if( materials.namePress().isSameAs( i ) )
				{
					return true;
				}

				for( final ItemStack optional : Api.INSTANCE.registries().inscriber().getOptionals() )
				{
					if( Platform.itemComparisons().isSameItem( i, optional ) )
					{
						return true;
					}
				}

				return false;

			case INSCRIBER_INPUT:
				return true;/*
							 * for (ItemStack is : Inscribe.inputs) if ( Platform.isSameItemPrecise( is, i ) ) return
							 * true;
							 * return false;
							 */

			case METAL_INGOTS:

				return isMetalIngot( i );

			case VIEW_CELL:
				return items.viewCell().isSameAs( i );
			case ORE:
				return Api.INSTANCE.registries().grinder().getRecipeForInput( i ) != null;
			case FUEL:
				return ForgeHooks.getBurnTime( i ) > 0;
			case POWERED_TOOL:
				return Platform.isChargeable( i );
			case QE_SINGULARITY:
				return materials.qESingularity().isSameAs( i );

			case RANGE_BOOSTER:
				return materials.wirelessBooster().isSameAs( i );

			case SPATIAL_STORAGE_CELLS:
				return i.getItem() instanceof ISpatialStorageCell && ( (ISpatialStorageCell) i.getItem() ).isSpatialStorage( i );
			case STORAGE_CELLS:
				return Api.INSTANCE.registries().cell().isCellHandled( i );
			case WORKBENCH_CELL:
				return i.getItem() instanceof ICellWorkbenchItem && ( (ICellWorkbenchItem) i.getItem() ).isEditable( i );
			case STORAGE_COMPONENT:
				return i.getItem() instanceof IStorageComponent && ( (IStorageComponent) i.getItem() ).isStorageComponent( i );
			case TRASH:
				if( Api.INSTANCE.registries().cell().isCellHandled( i ) )
				{
					return false;
				}

				return !( i.getItem() instanceof IStorageComponent && ( (IStorageComponent) i.getItem() ).isStorageComponent( i ) );
			case ENCODABLE_ITEM:
				return i.getItem() instanceof INetworkEncodable || Api.INSTANCE.registries().wireless().isWirelessTerminal( i );
			case BIOMETRIC_CARD:
				return i.getItem() instanceof IBiometricCard;
			case UPGRADES:
				return i.getItem() instanceof IUpgradeModule && ( (IUpgradeModule) i.getItem() ).getType( i ) != null;
			default:
				break;
		}

		return false;
	}

	@Override
	public boolean canTakeStack( final PlayerEntity par1PlayerEntity )
	{
		return this.isAllowEdit();
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if( Platform.isClient() && ( this.which == PlacableItemType.ENCODED_PATTERN ) )
		{
			final ItemStack is = super.getStack();
			// FIXME if( !is.isEmpty() && is.getItem() instanceof ItemEncodedPattern )
			// FIXME {
			// FIXME 	final ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
			// FIXME 	final ItemStack out = iep.getOutput( is );
			// FIXME 	if( !out.isEmpty() )
			// FIXME 	{
			// FIXME 		return out;
			// FIXME 	}
			// FIXME }
		}
		return super.getStack();
	}

	public static boolean isMetalIngot( final ItemStack i )
	{
		if( Platform.itemComparisons().isSameItem( i, new ItemStack( Items.IRON_INGOT ) ) )
		{
			return true;
		}

		for( final String name : new String[] { "Copper", "Tin", "Obsidian", "Iron", "Lead", "Bronze", "Brass", "Nickel", "Aluminium" } )
		{
			// FIXME for( final ItemStack ingot : OreDictionary.getOres( "ingot" + name ) )
			// FIXME {
			// FIXME 	if( Platform.itemComparisons().isSameItem( i, ingot ) )
			// FIXME 	{
			// FIXME 		return true;
			// FIXME 	}
			// FIXME }
		}

		return false;
	}

	private boolean isAllowEdit()
	{
		return this.allowEdit;
	}

	public void setAllowEdit( final boolean allowEdit )
	{
		this.allowEdit = allowEdit;
	}

	public enum PlacableItemType
	{
		STORAGE_CELLS( 15 ),
		ORE( 16 + 15 ),
		STORAGE_COMPONENT( 3 * 16 + 15 ),

		ENCODABLE_ITEM( 4 * 16 + 15 ),
		TRASH( 5 * 16 + 15 ),
		VALID_ENCODED_PATTERN_W_OUTPUT( 7 * 16 + 15 ),
		ENCODED_PATTERN_W_OUTPUT( 7 * 16 + 15 ),

		ENCODED_CRAFTING_PATTERN( 7 * 16 + 15 ),
		ENCODED_PATTERN( 7 * 16 + 15 ),
		PATTERN( 8 * 16 + 15 ),
		BLANK_PATTERN( 8 * 16 + 15 ),
		POWERED_TOOL( 9 * 16 + 15 ),

		RANGE_BOOSTER( 6 * 16 + 15 ),
		QE_SINGULARITY( 10 * 16 + 15 ),
		SPATIAL_STORAGE_CELLS( 11 * 16 + 15 ),

		FUEL( 12 * 16 + 15 ),
		UPGRADES( 13 * 16 + 15 ),
		WORKBENCH_CELL( 15 ),
		BIOMETRIC_CARD( 14 * 16 + 15 ),
		VIEW_CELL( 4 * 16 + 14 ),

		INSCRIBER_PLATE( 2 * 16 + 14 ),
		INSCRIBER_INPUT( 3 * 16 + 14 ),
		METAL_INGOTS( 3 * 16 + 14 );

		public final int IIcon;

		PlacableItemType( final int o )
		{
			this.IIcon = o;
		}
	}
}
