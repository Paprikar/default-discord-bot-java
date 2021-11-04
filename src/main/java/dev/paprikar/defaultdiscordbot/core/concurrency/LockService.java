package dev.paprikar.defaultdiscordbot.core.concurrency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LockService {

    // Map<ConcurrencyKey, Lock>
    private final Map<ConcurrencyKey, Lock> locks = new ConcurrentHashMap<>();

    @Autowired
    public LockService() {
    }

    public Lock putIfAbsent(@Nonnull ConcurrencyKey key, @Nonnull Lock lock) {
        return locks.putIfAbsent(key, lock);
    }

    public Lock putIfAbsent(@Nonnull ConcurrencyScope scope, @Nonnull Object key, @Nonnull Lock lock) {
        return putIfAbsent(ConcurrencyKey.from(scope, key), lock);
    }

    public Lock add(@Nonnull ConcurrencyKey key) {
        return locks.put(key, new ReentrantLock());
    }

    public Lock add(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return add(ConcurrencyKey.from(scope, key));
    }

    public Lock remove(@Nonnull ConcurrencyKey key) {
        return locks.remove(key);
    }

    public Lock remove(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return remove(ConcurrencyKey.from(scope, key));
    }

    public Lock get(@Nonnull ConcurrencyKey key) {
        return locks.get(key);
    }

    public Lock get(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return get(ConcurrencyKey.from(scope, key));
    }
}
