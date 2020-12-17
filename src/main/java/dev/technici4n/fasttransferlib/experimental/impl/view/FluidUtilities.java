package dev.technici4n.fasttransferlib.experimental.impl.view;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.transfer.Participant;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.impl.transfer.context.TransactionContext;

import java.util.Iterator;
import java.util.function.Predicate;

public enum FluidUtilities {
    ;

    public static long moveAll(View from, Participant to, Predicate<? super Content> filter) {
        long result = 0L;
        for (Iterator<? extends Atom> fromIterator = from.getAtomIterator();
             fromIterator.hasNext();) {
            Atom fromAtom = fromIterator.next();
            Content content = fromAtom.getContent();
            if (filter.test(content)) {
                while (true) {
                    long actualExtract;
                    try (TransactionContext transaction = new TransactionContext(2)) {
                        // first try - probe extract value
                        long extracted = fromAtom.extract(transaction, content, Long.MAX_VALUE);
                        long leftover = to.insert(transaction, content, extracted);
                        actualExtract = extracted - leftover;
                    }
                    if (actualExtract != 0L) {
                        try (TransactionContext transaction = new TransactionContext(2)) {
                            // second try - try extract, require match
                            long extracted = fromAtom.extract(transaction, content, actualExtract);
                            if (extracted == actualExtract) {
                                long leftover = to.insert(transaction, content, actualExtract);
                                if (leftover == 0L) {
                                    // matches, commit
                                    transaction.commit();
                                    result += actualExtract; // potential overflow, how to handle
                                    if (fromAtom.getAmount() > 0L)
                                        continue; // might be more to extract
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }
}
