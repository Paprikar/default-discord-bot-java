package dev.paprikar.defaultdiscordbot.core.concurrency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class SemaphoreService {

    // Map<ConcurrencyKey, Semaphore>
    private final Map<ConcurrencyKey, Semaphore> locks = new ConcurrentHashMap<>();

    @Autowired
    public SemaphoreService() {
    }

    public void add(@Nonnull ConcurrencyKey key) {
        add(key, 1);
    }

    public void add(@Nonnull ConcurrencyKey key, int permits) {
        locks.put(key, new Semaphore(permits));
    }

    public void add(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        add(scope, key, 1);
    }

    public void add(@Nonnull ConcurrencyScope scope, @Nonnull Object key, int permits) {
        locks.put(ConcurrencyKey.from(scope, key), new Semaphore(permits));
    }

    public void remove(@Nonnull ConcurrencyKey key) {
        locks.remove(key);
    }

    public void remove(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        remove(ConcurrencyKey.from(scope, key));
    }

    public Semaphore get(@Nonnull ConcurrencyKey key) {
        return locks.get(key);
    }

    public Semaphore get(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return get(ConcurrencyKey.from(scope, key));
    }
}
