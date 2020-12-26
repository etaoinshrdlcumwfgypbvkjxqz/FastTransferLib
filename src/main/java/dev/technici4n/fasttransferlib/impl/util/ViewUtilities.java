package dev.technici4n.fasttransferlib.impl.util;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.impl.context.TransactionContext;

import java.util.function.Function;

public enum ViewUtilities {
    ;

    public static long insert(View instance, Context context, Content content, long maxQuantity) {
        return insert(instance, context, maxQuantity, atom -> content);
    }

    public static long extract(View instance, Context context, Content content, long maxQuantity) {
        return extract(instance, context, maxQuantity, atom -> content);
    }

    public static long insert(View instance, Context context, long maxQuantity, Function<? super Atom, ? extends Content> filter) {
        try (TransactionContext transaction = new TransactionContext(instance.estimateAtomSize())) {
            // need transaction, extract and insert may have unknown side effect

            for (Atom instanceAtom : instance) {
                assert maxQuantity >= 0L;
                if (maxQuantity == 0L)
                    break;
                Content content = filter.apply(instanceAtom);
                if (content != null)
                    maxQuantity = instanceAtom.insert(transaction, content, maxQuantity);
            }

            transaction.commitWith(context); // commit using context
        }
        return maxQuantity;
    }

    public static long extract(View instance, Context context, long maxQuantity, Function<? super Atom, ? extends Content> filter) {
        long left = maxQuantity;
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
        return maxQuantity - left;
    }
}
