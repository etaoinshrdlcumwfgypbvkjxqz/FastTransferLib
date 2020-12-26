package dev.technici4n.fasttransferlib.impl.util;

import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.transfer.TransferAction;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.event.TransferEvent;
import dev.technici4n.fasttransferlib.impl.context.TransactionContext;
import dev.technici4n.fasttransferlib.impl.view.event.TransferEventImpl;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public enum TransferUtilities {
    ;

    public static long moveAll(Context context, View from, Participant to, Function<? super Atom, ? extends Content> filter) {
        long result = 0L;
        try (TransactionContext transaction = new TransactionContext(from.estimateAtomSize())) {
            // need transaction, extract and insert may have unknown side effect
            for (Atom fromAtom : from) {
                Content content = filter.apply(fromAtom);
                if (content != null) {
                    while (true) {
                        long actualExtract;
                        try (TransactionContext atomTransactionTest = new TransactionContext(2L)) {
                            // first try - probe extract value
                            long extracted = fromAtom.extract(atomTransactionTest, content, Long.MAX_VALUE);
                            long leftover = to.insert(atomTransactionTest, content, extracted);
                            actualExtract = extracted - leftover;
                            // rollback
                        }
                        if (actualExtract != 0L) {
                            try (TransactionContext atomTransaction = new TransactionContext(2L)) {
                                // second try - try extract, require match
                                long extracted = fromAtom.extract(transaction, content, actualExtract);
                                if (extracted == actualExtract) {
                                    long leftover = to.insert(transaction, content, actualExtract);
                                    if (leftover == 0L) {
                                        atomTransaction.commitWith(transaction);
                                        result += actualExtract; // potential overflow, how to handle
                                        if (fromAtom.getQuantity() > 0L)
                                            continue; // might be more to extract
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
            transaction.commitWith(context); // commit using context
        }
        return result;
    }

    public static long extractAll(Context context, Atom atom) {
        if (Atom.isEmpty(atom))
            return 0L;
        long result = 0L;
        try (TransactionContext transaction = new TransactionContext(2L)) {
            for (long quantity = atom.getQuantity(); quantity != 0L; quantity = atom.getQuantity()) {
                assert quantity > 0L;
                long extracted = atom.extract(transaction, atom.getContent(), quantity);
                if (extracted == 0L)
                    break;
                result += extracted;
            }
            transaction.commitWith(context);
        }
        return result;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Iterator<TransferEvent> compileToTransferData(Content from, BigInteger fromQuantity, Content to, BigInteger toQuantity) {
        if (from.isEmpty()) fromQuantity = BigInteger.ZERO;
        if (to.isEmpty()) toQuantity = BigInteger.ZERO;

        if (from.equals(to)) {
            // insert/extract
            TransferAction action;
            int comparison = toQuantity.compareTo(fromQuantity);
            if (comparison == 0) return Stream.<TransferEvent>empty().iterator(); // no delta
            else action = TransferAction.fromDifference(comparison > 0);

            BigInteger diff = toQuantity.subtract(fromQuantity);

            return BigIntegerAsLongIterator.ofStream(diff.abs())
                    .<TransferEvent>mapToObj(toQuantity1 -> TransferEventImpl.of(action, from, toQuantity1))
                    .iterator();
        } else {
            // extract and insert
            return Streams.concat(
                    BigIntegerAsLongIterator.ofStream(fromQuantity)
                            .<TransferEvent>mapToObj(toQuantity1 -> TransferEventImpl.ofExtraction(from, toQuantity1)),
                    BigIntegerAsLongIterator.ofStream(toQuantity)
                            .<TransferEvent>mapToObj(toQuantity1 -> TransferEventImpl.ofInsertion(from, toQuantity1))
            ).iterator();
        }
    }

    public static boolean setAtomContent(Context context, Atom atom, Content content, BigInteger quantity) {
        if (content.isEmpty()) quantity = BigInteger.ZERO;

        if (content.equals(atom.getContent())) {
            // insert/extract
            BigInteger atomQuantity = BigInteger.valueOf(atom.getQuantity());

            boolean extract;
            int comparison = atomQuantity.compareTo(quantity);
            if (comparison > 0) extract = true;
            else if (comparison < 0) extract = false;
            else return true;

            BigInteger diff = atomQuantity.subtract(quantity);

            try (TransactionContext transaction = new TransactionContext(1L)) {
                if (BigIntegerAsLongIterator.ofStream(diff.abs())
                        .map(extract
                                ? diff1 -> atom.extract(transaction, content, diff1) - diff1
                                : diff1 -> atom.insert(transaction, content, diff1))
                        .allMatch(result -> result == 0L)) {
                    transaction.commitWith(context);
                    return true;
                }
                return false;
            }
        } else {
            // extract and insert
            try (TransactionContext transaction = new TransactionContext(2L)) {
                TransferUtilities.extractAll(transaction, atom);
                if (Atom.isEmpty(atom)
                        && BigIntegerAsLongIterator.ofStream(quantity)
                        .map(quantity1 -> atom.insert(transaction, content, quantity1))
                        .allMatch(leftover -> leftover == 0L)) {
                    transaction.commitWith(context);
                    return true;
                }
                return false;
            }
        }
    }

    public static class BigIntegerAsLongIterator
            implements LongIterator {
        public static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
        public static final BigInteger LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
        private BigInteger integer;

        protected BigIntegerAsLongIterator(BigInteger integer) {
            this.integer = integer;
        }

        public static BigIntegerAsLongIterator of(BigInteger integer) {
            return new BigIntegerAsLongIterator(integer);
        }

        public static Spliterator.OfLong ofSpliterator(BigInteger integer) {
            return Spliterators.spliteratorUnknownSize(of(integer),
                    Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public static LongStream ofStream(BigInteger integer) {
            return StreamSupport.longStream(ofSpliterator(integer), false);
        }

        @Override
        public long nextLong() {
            BigInteger integer = getInteger();
            BigInteger result;
            switch (integer.signum()) {
                case 1:
                    result = integer.min(LONG_MAX_VALUE);
                    break;
                case -1:
                    result = integer.max(LONG_MIN_VALUE);
                    break;
                case 0:
                    throw new NoSuchElementException();
                default:
                    throw new AssertionError();
            }
            setInteger(integer.subtract(result));
            return result.longValueExact();
        }

        @Override
        public boolean hasNext() {
            return getInteger().signum() != 0;
        }

        protected BigInteger getInteger() {
            return integer;
        }

        protected void setInteger(BigInteger integer) {
            this.integer = integer;
        }
    }
}
