package dev.technici4n.fasttransferlib.experimental.impl.util;

import dev.technici4n.fasttransferlib.experimental.api.Content;
import dev.technici4n.fasttransferlib.experimental.api.Context;
import dev.technici4n.fasttransferlib.experimental.api.view.Atom;
import dev.technici4n.fasttransferlib.experimental.api.view.View;
import dev.technici4n.fasttransferlib.experimental.impl.context.TransactionContext;

import java.util.Iterator;
import java.util.function.Function;

public enum ViewUtilities {
    ;

    public static long insert(View instance, Context context, Content content, long maxAmount) {
        return insert(instance, context, maxAmount, atom -> content);
    }

    public static long extract(View instance, Context context, Content content, long maxAmount) {
        return extract(instance, context, maxAmount, atom -> content);
    }

    public static long insert(View instance, Context context, long maxAmount, Function<? super Atom, ? extends Content> filter) {
        try (TransactionContext transaction = new TransactionContext(instance.estimateAtomSize())) {
            // need transaction, extract and insert may have unknown side effect

            for (Iterator<? extends Atom> instanceIterator = instance.getAtomIterator();
                 instanceIterator.hasNext();) {
                assert maxAmount >= 0L;
                if (maxAmount == 0L)
                    break;
                Atom instanceAtom = instanceIterator.next();
                Content content = filter.apply(instanceAtom);
                if (content != null)
                    maxAmount = instanceAtom.insert(transaction, content, maxAmount);
            }

            transaction.commitWith(context); // commit using context
        }
        return maxAmount;
    }

    public static long extract(View instance, Context context, long maxAmount, Function<? super Atom, ? extends Content> filter) {
        long left = maxAmount;
        try (TransactionContext transaction = new TransactionContext(instance.estimateAtomSize())) {
            // need transaction, extract and insert may have unknown side effect

            for (Iterator<? extends Atom> instanceIterator = instance.getAtomIterator();
                 instanceIterator.hasNext();) {
                assert left >= 0L;
                if (left == 0L)
                    break;
                Atom instanceAtom = instanceIterator.next();
                Content content = filter.apply(instanceAtom);
                if (content != null)
                    left -= instanceAtom.extract(transaction, content, left);
            }

            transaction.commitWith(context); // commit using context
        }
        return maxAmount - left;
    }
}
