package dev.technici4n.fasttransferlib.experimental.api.view;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;
import dev.technici4n.fasttransferlib.experimental.api.view.model.Model;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Iterator;

public interface Atom
        extends View, Participant, Model {
    Content getContent();

    long getAmount();

    @Override
    default Iterator<? extends Atom> getAtomIterator() {
        return Iterators.singletonIterator(this);
    }

    @Override
    default long getAtomSize() {
        return 1L;
    }

    @Override
    default long estimateAtomSize() {
        return getAtomSize();
    }

    @Override
    default long getAmount(Content content) {
        if (getContent().equals(content))
            return getAmount();
        return 0L;
    }

    @Override
    default Object2LongMap<Content> getAmounts() {
        return Object2LongMaps.unmodifiable(new Object2LongOpenHashMap<>(ImmutableMap.of(getContent(), getAmount())));
    }

    @Override
    default Model getDirectModel() {
        return this;
    }
}
