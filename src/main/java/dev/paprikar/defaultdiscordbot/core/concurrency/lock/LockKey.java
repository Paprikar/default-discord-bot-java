package dev.paprikar.defaultdiscordbot.core.concurrency.lock;

import javax.annotation.Nonnull;

public class LockKey<T extends Enum<T>> {

    private final T scope;

    private final Object lockKey;

    private LockKey(T scope, Object lockKey) {
        this.scope = scope;
        this.lockKey = lockKey;
    }

    @Nonnull
    public static <T extends Enum<T>> LockKey<T> from(@Nonnull T scope, @Nonnull Object lockKey) {
        return new LockKey<>(scope, lockKey);
    }

    @Nonnull
    public T getScope() {
        return scope;
    }

    @Nonnull
    public Object getLockKey() {
        return lockKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LockKey<?> lockKey1 = (LockKey<?>) o;

        if (!scope.equals(lockKey1.scope)) {
            return false;
        }
        return lockKey.equals(lockKey1.lockKey);
    }

    @Override
    public int hashCode() {
        int result = scope.hashCode();
        result = 31 * result + lockKey.hashCode();
        return result;
    }
}
