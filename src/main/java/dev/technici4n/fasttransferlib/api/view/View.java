package dev.technici4n.fasttransferlib.api.view;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import dev.technici4n.fasttransferlib.api.view.observer.Subscription;
import dev.technici4n.fasttransferlib.api.view.observer.TransferData;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface View {
    Iterator<? extends Atom> getAtomIterator();

    long getAtomSize();

    long estimateAtomSize();

    long getAmount(Content content);

    Object2LongMap<Content> getAmounts();

    Set<? extends Content> getContents();

    Model getDirectModel();

    Object getRevision();

    Optional<? extends Subscription> addObserver(Consumer<? super TransferData> observer);
}
