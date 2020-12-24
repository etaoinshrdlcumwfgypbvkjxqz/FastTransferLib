package dev.technici4n.fasttransferlib.impl.compat.tr;

import dev.technici4n.fasttransferlib.api.content.ContentApi;
import dev.technici4n.fasttransferlib.api.lookup.BlockLookupContext;
import dev.technici4n.fasttransferlib.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.api.view.ViewApi;
import dev.technici4n.fasttransferlib.impl.compat.tr.energy.TrEnergyHandlerToViewParticipant;
import dev.technici4n.fasttransferlib.impl.compat.tr.energy.TrEnergyType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.Energy;

public enum TrCompat {
    ;

    public enum EnergyCompat {
        ;

        static {
            ContentApi.ENERGY_DESERIALIZERS.put(TrEnergyType.INSTANCE.getIdentifier(), TrEnergyType.INSTANCE);

            TransferApi.BLOCK.registerFallback(EnergyCompat::getBlockViewParticipantFallback);
            ViewApi.BLOCK.registerFallback(EnergyCompat::getBlockViewParticipantFallback);
        }

        @SuppressWarnings("unused")
        public static void initializeClass() {}

        private static TrEnergyHandlerToViewParticipant getBlockViewParticipantFallback(World world, BlockPos pos, BlockState state, @Nullable BlockEntity entity, BlockLookupContext context) {
            if (entity != null && Energy.valid(entity)) {
                return TrEnergyHandlerToViewParticipant.of(Energy.of(entity).side(context.getDirection()));
            }

            return null;
        }
    }
}
