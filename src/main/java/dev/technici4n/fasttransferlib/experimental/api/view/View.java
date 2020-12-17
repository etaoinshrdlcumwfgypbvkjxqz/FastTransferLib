package dev.technici4n.fasttransferlib.experimental.api.view;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.view.model.Model;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Iterator;

public interface View {
    Iterator<? extends Atom> getAtomIterator();

    long getAtomSize();

    long estimateAtomSize();

    long getAmount(Content content);

    Object2LongMap<Content> getAmounts();

    Model getDirectModel();
}
