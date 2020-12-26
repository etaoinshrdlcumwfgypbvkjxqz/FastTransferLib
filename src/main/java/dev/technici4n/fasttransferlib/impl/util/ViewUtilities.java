package dev.technici4n.fasttransferlib.impl.util;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.impl.context.TransactionContext;

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

            for (Atom instanceAtom : instance) {
                assert maxAmount >= 0L;
                if (maxAmount == 0L)
                    break;
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

            for (Atom instanceAtom : instance) {
                assert left >= 0L;
                if (left == 0L)
                    break;
                Content content = filter.apply(instanceAtom);
                if (content != null)
                    left -= instanceAtom.extract(transaction, content, left);
            }

            transaction.commitWith(context); // commit using context
        }
        return maxAmount - left;
    }
}
