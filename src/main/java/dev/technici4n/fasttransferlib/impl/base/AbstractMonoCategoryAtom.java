package dev.technici4n.fasttransferlib.impl.base;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.Atom;

public abstract class AbstractMonoCategoryAtom<T>
        extends AbstractMonoCategoryViewParticipant<T>
        implements Atom {
    protected AbstractMonoCategoryAtom(Class<T> category) {
        super(category);
    }

    @Override
    protected long getAmount(Content content, T type) {
        return Atom.super.getAmount(content);
    }
}
