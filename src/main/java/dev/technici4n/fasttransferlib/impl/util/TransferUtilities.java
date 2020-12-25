package dev.technici4n.fasttransferlib.impl.util;

import com.google.common.collect.Streams;
import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.context.Context;
import dev.technici4n.fasttransferlib.api.transfer.Participant;
import dev.technici4n.fasttransferlib.api.view.Atom;
import dev.technici4n.fasttransferlib.api.view.View;
import dev.technici4n.fasttransferlib.api.view.flow.TransferData;
import dev.technici4n.fasttransferlib.impl.context.TransactionContext;
import dev.technici4n.fasttransferlib.impl.view.flow.TransferDataImpl;
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

            for (Iterator<? extends Atom> fromIterator = from.getAtomIterator();
                 fromIterator.hasNext();) {
                Atom fromAtom = fromIterator.next();
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

            transaction.commitWith(context); // commit using context
        }
        return result;
    }

    public static long extractAll(Context context, Atom atom) {
        if (Atom.isEmpty(atom))
            return 0L;
        long result = 0L;
        try (TransactionContext transaction = new TransactionContext(2L)) {
            for (long amount = atom.getAmount(); amount != 0L; amount = atom.getAmount()) {
                assert amount > 0L;
                long extracted = atom.extract(transaction, atom.getContent(), amount);
                if (extracted == 0L)
                    break;
                result += extracted;
            }
            transaction.commitWith(context);
        }
        return result;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Iterator<TransferData> compileToTransferData(Content from, BigInteger fromAmount, Content to, BigInteger toAmount) {
        if (from.isEmpty()) fromAmount = BigInteger.ZERO;
        if (to.isEmpty()) toAmount = BigInteger.ZERO;

        if (from.equals(to)) {
            // insert/extract
            TransferData.Type type;
            int comparison = toAmount.compareTo(fromAmount);
            if (comparison == 0) return Stream.<TransferData>empty().iterator(); // no delta
            else type = TransferData.Type.fromDifference(comparison > 0);

            BigInteger diff = toAmount.subtract(fromAmount);

            return BigIntegerAsLongIterator.ofStream(diff.abs())
                    .<TransferData>mapToObj(toAmount1 -> TransferDataImpl.of(type, from, toAmount1))
                    .iterator();
        } else {
            // extract and insert
            return Streams.concat(
                    BigIntegerAsLongIterator.ofStream(fromAmount)
                            .<TransferData>mapToObj(toAmount1 -> TransferDataImpl.ofExtraction(from, toAmount1)),
                    BigIntegerAsLongIterator.ofStream(toAmount)
                            .<TransferData>mapToObj(toAmount1 -> TransferDataImpl.ofInsertion(from, toAmount1))
            ).iterator();
        }
    }

    public static boolean setAtomContent(Context context, Atom atom, Content content, BigInteger amount) {
        if (content.isEmpty()) amount = BigInteger.ZERO;

        if (content.equals(atom.getContent())) {
            // insert/extract
            BigInteger atomAmount = BigInteger.valueOf(atom.getAmount());

            boolean extract;
            int comparison = atomAmount.compareTo(amount);
            if (comparison > 0) extract = true;
            else if (comparison < 0) extract = false;
            else return true;

            BigInteger diff = atomAmount.subtract(amount);

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
                        && BigIntegerAsLongIterator.ofStream(amount)
                        .map(amount1 -> atom.insert(transaction, content, amount1))
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
