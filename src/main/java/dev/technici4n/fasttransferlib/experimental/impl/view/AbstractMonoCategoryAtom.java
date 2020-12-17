package dev.technici4n.fasttransferlib.experimental.impl.view;

import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.participant.AbstractMonoCategoryParticipant;

public abstract class AbstractMonoCategoryAtom<T>
        extends AbstractMonoCategoryParticipant<T>
        implements Atom {
    protected AbstractMonoCategoryAtom(Class<T> category) {
        super(category);
    }
}
