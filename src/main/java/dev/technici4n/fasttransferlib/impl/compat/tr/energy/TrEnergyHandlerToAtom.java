package dev.technici4n.fasttransferlib.impl.compat.tr.energy;

import com.google.common.collect.ImmutableSet;
import com.google.common.math.DoubleMath;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.content.EnergyType;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.impl.base.AbstractMonoCategoryAtom;
import dev.technici4n.fasttransferlib.impl.content.EmptyContent;
import dev.technici4n.fasttransferlib.impl.content.EnergyContent;
import dev.technici4n.fasttransferlib.impl.mixin.EnergyHandlerAccess;
import dev.technici4n.fasttransferlib.impl.util.NumberUtilities;
import team.reborn.energy.EnergyHandler;
import team.reborn.energy.EnergyStorage;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.OptionalLong;

public class TrEnergyHandlerToAtom
        extends AbstractMonoCategoryAtom<EnergyType>
        implements View {
    private final EnergyHandler delegate;

    protected TrEnergyHandlerToAtom(EnergyHandler delegate) {
        super(EnergyType.class);
        this.delegate = delegate.simulate();
    }

    public static TrEnergyHandlerToAtom of(EnergyHandler delegate) {
        return new TrEnergyHandlerToAtom(delegate);
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
        return getAmount() == 0L ? EmptyContent.INSTANCE : EnergyContent.of(TrEnergyType.INSTANCE);
    }

    @Override
    public long getAmount() {
        return NumberUtilities.toSaturatedInteger(getDelegate().getEnergy());
    }

    @Override
    public OptionalLong getCapacity() {
        return OptionalLong.of(NumberUtilities.toSaturatedInteger(getDelegate().getMaxStored()));
    }

    @Override
    protected long insertCurrent(Context context, long maxAmount) {
        /* note
        This assumes that insertion can be reverted by a following extraction.
        This seems to be the case for 'EnergyHandler'.

        Nevermind, just hack around it.

        Why is 'Nevermind' a typo?
         */

        EnergyHandler delegate = getDelegate();
        double current = delegate.getEnergy();
        double inserted = delegate.insert(maxAmount); // sim

        EnergyStorage delegateStorage = getDelegateAccess().getHolder();
        context.configure(() -> delegateStorage.setStored(current + inserted), () -> delegateStorage.setStored(current));

        return maxAmount - DoubleMath.roundToLong(inserted, RoundingMode.UP);
    }

    @Override
    protected long insertNew(Context context, Content content, EnergyType type, long maxAmount) {
        if (!content.equals(EnergyContent.of(TrEnergyType.INSTANCE)))
            return maxAmount;
        return insertCurrent(context, maxAmount);
    }

    @Override
    protected long extractCurrent(Context context, long maxAmount) {
        /* note
        This assumes that extraction can be reverted by a following insertion.
        This seems to be the case for 'EnergyHandler'.

        Nevermind, just hack around it.

        Why is 'Nevermind' a typo?
         */

        EnergyHandler delegate = getDelegate();
        double current = delegate.getEnergy();
        double extracted = delegate.extract(maxAmount); // sim

        EnergyStorage delegateStorage = getDelegateAccess().getHolder();
        context.configure(() -> delegateStorage.setStored(current - extracted), () -> delegateStorage.setStored(current));

        return DoubleMath.roundToLong(extracted, RoundingMode.DOWN);
    }

    @Override
    public Object getRevision() {
        assert supportsPullNotification();
        return getDelegate().getEnergy();
    }

    @Override
    protected Collection<? extends Class<?>> getSupportedPushNotifications() {
        return ImmutableSet.of(); // energy storage
    }

    @Override
    protected boolean supportsPullNotification() {
        return true;
    }
}
