package dev.technici4n.fasttransferlib.experimental.impl.compat.tr;

import dev.technici4n.fasttransferlib.experimental.api.content.ContentApi;
import dev.technici4n.fasttransferlib.experimental.api.transfer.TransferApi;
import dev.technici4n.fasttransferlib.experimental.impl.compat.tr.energy.TrEnergyHandlerToViewParticipant;
import dev.technici4n.fasttransferlib.experimental.impl.compat.tr.energy.TrEnergyType;
import team.reborn.energy.Energy;

public enum TrCompat {
    ;

    public enum EnergyCompat {
        ;

        static {
            ContentApi.ENERGY_DESERIALIZERS.put(TrEnergyType.INSTANCE.getIdentifier(), TrEnergyType.INSTANCE);

            TransferApi.BLOCK.registerBlockEntityFallback((blockEntity, context) -> {
                if (Energy.valid(blockEntity)) {
                    return TrEnergyHandlerToViewParticipant.of(Energy.of(blockEntity).side(context.getDirection()));
                }

                return null;
            });
        }

        @SuppressWarnings("unused")
        public static void initializeClass() {}
    }
}
