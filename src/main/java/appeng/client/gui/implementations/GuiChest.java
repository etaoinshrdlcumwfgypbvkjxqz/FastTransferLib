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

package appeng.client.gui.implementations;


import appeng.container.implementations.ContainerPriority;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerChest;
import appeng.core.localization.GuiText;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;


public class GuiChest extends AEBaseGui<ContainerChest>
{

	private GuiTabButton priority;

	public GuiChest(ContainerChest container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 166;
	}

	protected void actionPerformed( final Button button )
	{
		if( button == this.priority )
		{
			NetworkHandler.instance().sendToServer( new PacketSwitchGuis( ContainerPriority.TYPE ) );
		}
	}

	@Override
	public void init()
	{
		super.init();

		this.addButton( this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRenderer, this::actionPerformed ) );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( GuiText.Chest.getLocal() ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks)
	{
		this.bindTexture( "guis/chest.png" );
		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize, getBlitOffset() );
	}
}
