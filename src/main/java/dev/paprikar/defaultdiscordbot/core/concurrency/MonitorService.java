package dev.paprikar.defaultdiscordbot.core.concurrency;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitorService {

    // Map<ConcurrencyKey, Monitor>
    private final Map<ConcurrencyKey, Object> locks = new ConcurrentHashMap<>();

    public Object get(@Nonnull ConcurrencyKey key) {
        return locks.get(key);
    }

    public Object get(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return get(ConcurrencyKey.from(scope, key));
    }

    public Object add(@Nonnull ConcurrencyKey key) {
        return locks.put(key, new Object());
    }

    public Object add(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return add(ConcurrencyKey.from(scope, key));
    }

    public Object putIfAbsent(@Nonnull ConcurrencyKey key, @Nonnull Object monitor) {
        return locks.putIfAbsent(key, monitor);
    }

    public Object putIfAbsent(@Nonnull ConcurrencyScope scope, @Nonnull Object key, @Nonnull Object monitor) {
        return putIfAbsent(ConcurrencyKey.from(scope, key), monitor);
    }

    public Object remove(@Nonnull ConcurrencyKey key) {
        return locks.remove(key);
    }

    public Object remove(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return remove(ConcurrencyKey.from(scope, key));
    }
}
