package dev.paprikar.defaultdiscordbot.core.concurrency;

import javax.annotation.Nonnull;

/**
 * A composite key for working with concurrency objects.
 */
public class ConcurrencyKey {

    private final ConcurrencyScope scope;

    private final Object key;

    private ConcurrencyKey(ConcurrencyScope scope, Object key) {
        this.scope = scope;
        this.key = key;
    }

    /**
     * Constructs a key from the concurrency scope and object.
     *
     * @param scope
     *         the scope of concurrency
     * @param key
     *         the object for the key composition
     *
     * @return the concurrency key
     */
    public static ConcurrencyKey from(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return new ConcurrencyKey(scope, key);
    }

    /**
     * @return the scope of concurrency
     */
    public ConcurrencyScope getScope() {
        return scope;
    }

    /**
     * @return the object for the key composition
     */
    public Object getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConcurrencyKey that = (ConcurrencyKey) o;

        if (scope != that.scope) {
            return false;
        }
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        int result = scope.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }
}
