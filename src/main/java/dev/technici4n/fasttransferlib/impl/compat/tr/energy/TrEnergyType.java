package dev.technici4n.fasttransferlib.impl.compat.tr.energy;

import dev.technici4n.fasttransferlib.api.content.EnergyType;
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
