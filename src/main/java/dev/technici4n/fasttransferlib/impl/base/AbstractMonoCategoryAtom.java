package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.view.Atom;

public abstract class AbstractMonoCategoryAtom<T>
        extends AbstractMonoCategoryParticipant<T>
        implements Atom {
    protected AbstractMonoCategoryAtom(Class<T> category) {
        super(category);
    }
}
