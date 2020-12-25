package dev.technici4n.fasttransferlib.impl.util;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Optional;

public class OptionalWeakReference<T>
        extends WeakReference<T> {
    private static final OptionalWeakReference<?> NULL = new OptionalWeakReference<>(null, null);

    protected OptionalWeakReference(T referent, @Nullable ReferenceQueue<? super T> queue) {
        super(referent, queue);
    }

    @SuppressWarnings("unchecked")
    public static <T> OptionalWeakReference<T> of(T referent, @Nullable ReferenceQueue<? super T> queue) {
        if (referent == null && queue == null)
            return (OptionalWeakReference<T>) NULL;
        return new OptionalWeakReference<>(referent, queue);
    }

    public static <T> OptionalWeakReference<T> of(T referent) {
        return of(referent, null);
    }

    public Optional<? extends T> getOptional() {
        return Optional.ofNullable(get());
    }
}
