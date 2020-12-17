package dev.technici4n.fasttransferlib.experimental.impl.base;

import dev.technici4n.fasttransferlib.experimental.api.view.Atom;

public abstract class AbstractMonoCategoryAtom<T>
        extends AbstractMonoCategoryParticipant<T>
        implements Atom {
    protected AbstractMonoCategoryAtom(Class<T> category) {
        super(category);
    }
}
