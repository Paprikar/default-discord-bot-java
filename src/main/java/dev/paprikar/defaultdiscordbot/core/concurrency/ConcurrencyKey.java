package dev.paprikar.defaultdiscordbot.core.concurrency;

import javax.annotation.Nonnull;

public class ConcurrencyKey {

    private final ConcurrencyScope scope;

    private final Object key;

    private ConcurrencyKey(ConcurrencyScope scope, Object key) {
        this.scope = scope;
        this.key = key;
    }

    public static ConcurrencyKey from(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return new ConcurrencyKey(scope, key);
    }

    public ConcurrencyScope getScope() {
        return scope;
    }

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
