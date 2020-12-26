package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.query.Query;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.flow.Publisher;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.fabricmc.fabric.api.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractComposedViewParticipant
        implements View, Participant {
    protected abstract View getView();

    protected abstract Participant getParticipant();

    @Override
    public long insert(Context context, Content content, long maxAmount) {
        return getParticipant().insert(context, content, maxAmount);
    }

    @Override
    public long extract(Context context, Content content, long maxAmount) {
        return getParticipant().extract(context, content, maxAmount);
    }

    @Override
    public @NotNull Iterator<Atom> iterator() {
        return getView().iterator();
    }

    @Override
    public long getAtomSize() {
        return getView().getAtomSize();
    }

    @Override
    public long estimateAtomSize() {
        return getView().estimateAtomSize();
    }

    @Override
    public long getAmount(Content content) {
        return getView().getAmount(content);
    }

    @Override
    public Object2LongMap<Content> getAmounts() {
        return getView().getAmounts();
    }

    @Override
    public Set<Content> getContents() {
        return getView().getContents();
    }

    @Override
    public Model getDirectModel() {
        return getView().getDirectModel();
    }

    @Override
    public Object getRevision() {
        return getView().getRevision();
    }

    @Override
    public <T> Optional<? extends Publisher<T>> getPublisher(Class<T> discriminator) {
        return getView().getPublisher(discriminator);
    }

    @Override
    public TriState query(Query query) {
        TriState viewQuery = getView().query(query);
        TriState participantQuery = getParticipant().query(query);
        if (viewQuery == participantQuery)
            return viewQuery;
        if (viewQuery == TriState.TRUE && participantQuery == TriState.DEFAULT
                || participantQuery == TriState.TRUE && viewQuery == TriState.DEFAULT)
            return TriState.TRUE;
        /* table
        TRUE TRUE -> TRUE
        DEFAULT TRUE -> TRUE

        FALSE FALSE -> FALSE

        DEFAULT DEFAULT -> DEFAULT
        DEFAULT FALSE -> DEFAULT
        TRUE FALSE -> DEFAULT
         */
        return TriState.DEFAULT;
    }
}
