package dev.technici4n.fasttransferlib.api.view;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.model.Model;
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
