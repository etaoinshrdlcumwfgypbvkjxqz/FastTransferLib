package dev.technici4n.fasttransferlib.experimental.impl.compat.tr.energy;

import dev.technici4n.fasttransferlib.experimental.api.content.energy.EnergyType;
import net.minecraft.util.Identifier;

public enum TrEnergyType
        implements EnergyType {
    INSTANCE,
    ;

    public static final Identifier IDENTIFIER = new Identifier("techreborn", "energy");

    @Override
    public Identifier getIdentifier() {
        return IDENTIFIER;
    }
}
