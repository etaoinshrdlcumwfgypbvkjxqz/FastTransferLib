package dev.technici4n.fasttransferlib.api.view;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.query.Queryable;
import dev.technici4n.fasttransferlib.api.view.flow.Publisher;
import dev.technici4n.fasttransferlib.api.view.model.Model;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Optional;
import java.util.Set;

public interface View
        extends Iterable<Atom>, Queryable {
    long getAtomSize();

    long estimateAtomSize();

    long getAmount(Content content);

    Object2LongMap<Content> getAmounts();

    Set<Content> getContents();

    Model getDirectModel();

    Object getRevision();

    <T> Optional<? extends Publisher<T>> getPublisher(Class<T> discriminator);
}
