/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.parts;

import java.math.RoundingMode;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.attributes.MEAttributes;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;

/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public class FluidExportBusPart extends SharedFluidBusPart {
    public static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "part/fluid_export_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/fluid_export_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/fluid_export_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/fluid_export_bus_has_channel"));

    private final IActionSource source;

    public FluidExportBusPart(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        this.source = new MachineSource(this);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.FluidExportBus.getMin(), TickRates.FluidExportBus.getMax(),
                this.isSleeping(), false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        FluidInsertable insertable = MEAttributes.getAttributeInFrontOfPart(FluidAttributes.INSERTABLE, this);

        if (insertable != null) {
            try {
                final IMEMonitor<IAEFluidStack> inv = this.getProxy().getStorage().getInventory(this.getChannel());

                for (int i = 0; i < this.getConfig().getSlots(); i++) {
                    IAEFluidStack fluid = this.getConfig().getFluidInSlot(i);
                    if (fluid != null) {
                        final IAEFluidStack toExtract = fluid.copy();

                        toExtract.setStackSize(this.calculateAmountToSend());

                        final IAEFluidStack out = inv.extractItems(toExtract, Actionable.SIMULATE, this.source);

                        if (out != null) {
                            FluidVolume toInsert = out.getFluidStack();
                            FluidVolume remainder = insertable.attemptInsertion(toInsert, Simulation.ACTION);

                            if (remainder.getAmount_F().isLessThan(toInsert.getAmount_F())) {
                                // This will loose some liquid if the target accepts 1/1001'th bucket of fluid,
                                // we'll deduct 1/1000th from storage.
                                long remainderMillibuckets = remainder.getAmount_F().asInt(1000, RoundingMode.DOWN);

                                toExtract.setStackSize(remainderMillibuckets);
                                inv.extractItems(toExtract, Actionable.MODULATE, this.source);

                                return TickRateModulation.FASTER;
                            }
                        }
                    }
                }

                return TickRateModulation.SLOWER;
            } catch (GridAccessException e) {
                // Ignore
            }
        }

        return TickRateModulation.SLEEP;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }
}
