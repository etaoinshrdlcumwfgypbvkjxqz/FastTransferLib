package dev.technici4n.fasttransferlib.api.view;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.OptionalLong;
import java.util.Set;

public interface Atom
        extends View, Participant, Model {
    Content getContent();

    long getQuantity();

    OptionalLong getCapacity();

    @Override
    default @NotNull Iterator<Atom> iterator() {
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
    default long getQuantity(Content content) {
        if (getContent().equals(content))
            return getQuantity();
        return 0L;
    }

    @Override
    default Object2LongMap<Content> getQuantities() {
        return Object2LongMaps.unmodifiable(new Object2LongOpenHashMap<>(ImmutableMap.of(getContent(), getQuantity())));
    }

    @Override
    default Set<Content> getContents() {
        return ImmutableSet.of(getContent());
    }

    @Override
    default Model getDirectModel() {
        return this;
    }

    static boolean isEmpty(Atom instance) {
        // do NOT use #getContent to test emptiness
        return instance.getQuantity() == 0L;
    }
}
