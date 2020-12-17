package dev.technici4n.fasttransferlib.experimental.impl.compat.tr.energy;

import com.google.common.math.DoubleMath;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.content.Content;
import dev.technici4n.fasttransferlib.experimental.api.content.Energy;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.experimental.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.experimental.impl.content.EnergyContent;
import dev.technici4n.fasttransferlib.experimental.impl.util.NumberUtilities;
import dev.technici4n.fasttransferlib.impl.mixin.EnergyHandlerAccess;
import team.reborn.energy.EnergyHandler;
import team.reborn.energy.EnergyStorage;

import java.math.RoundingMode;

public class TrEnergyHandlerToViewParticipant
        extends AbstractMonoCategoryAtom<Energy>
        implements View {
    private final EnergyHandler delegate;

    protected TrEnergyHandlerToViewParticipant(EnergyHandler delegate) {
        super(Energy.class);
        this.delegate = delegate.simulate();
    }

    public static TrEnergyHandlerToViewParticipant of(EnergyHandler delegate) {
        return new TrEnergyHandlerToViewParticipant(delegate);
    }

    protected EnergyHandler getDelegate() {
        return delegate;
    }

    @SuppressWarnings("ConstantConditions")
    protected EnergyHandlerAccess getDelegateAccess() {
        return (EnergyHandlerAccess) (Object) getDelegate();
    }

    @Override
    public Content getContent() {
        return getAmount() == 0L ? EmptyContent.INSTANCE : EnergyContent.of(TrEnergy.INSTANCE);
    }

    @Override
    public long getAmount() {
        return NumberUtilities.toSaturatedInteger(getDelegate().getEnergy());
    }

    @Override
    protected long insert(Context context, Content content, Energy type, long maxAmount) {
        if (!content.equals(EnergyContent.of(TrEnergy.INSTANCE)))
            return maxAmount;

        /* note
        This assumes that insertion can be reverted by a following extraction.
        This seems to be the case for 'EnergyHandler'.

        Nevermind, just hack around it.
         */

        EnergyHandler delegate = getDelegate();
        double current = delegate.getEnergy();
        double inserted = delegate.insert(maxAmount); // sim

        EnergyStorage delegateStorage = getDelegateAccess().getHolder();
        context.execute(() -> delegateStorage.setStored(current + inserted), () -> delegateStorage.setStored(current));

        return maxAmount - DoubleMath.roundToLong(inserted, RoundingMode.UP);
    }

    @Override
    protected long extract(Context context, Content content, Energy type, long maxAmount) {
        if (!content.equals(EnergyContent.of(TrEnergy.INSTANCE)))
            return 0L;

        /* note
        This assumes that extraction can be reverted by a following insertion.
        This seems to be the case for 'EnergyHandler'.

        Nevermind, just hack around it.
         */

        EnergyHandler delegate = getDelegate();
        double current = delegate.getEnergy();
        double extracted = delegate.extract(maxAmount); // sim

        EnergyStorage delegateStorage = getDelegateAccess().getHolder();
        context.execute(() -> delegateStorage.setStored(current - extracted), () -> delegateStorage.setStored(current));

        return DoubleMath.roundToLong(extracted, RoundingMode.DOWN);
    }
}
