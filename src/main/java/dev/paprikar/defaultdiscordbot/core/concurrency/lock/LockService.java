package dev.paprikar.defaultdiscordbot.core.concurrency.lock;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LockService {

    // Map<LockKey, Lock>
    private final Map<LockKey<LockScope>, Lock> locks = new ConcurrentHashMap<>();

    public LockService() {
    }

    public void add(@Nonnull LockKey<LockScope> key) {
        locks.put(key, new ReentrantLock());
    }

    public void add(@Nonnull LockScope scope, @Nonnull Object lockKey) {
        locks.put(LockKey.from(scope, lockKey), new ReentrantLock());
    }

    public void remove(@Nonnull LockKey<LockScope> key) {
        locks.remove(key);
    }

    public void remove(@Nonnull LockScope scope, @Nonnull Object lockKey) {
        locks.remove(LockKey.from(scope, lockKey));
    }

    public Lock get(@Nonnull LockKey<LockScope> key) {
        return locks.get(key);
    }

    public Lock get(@Nonnull LockScope scope, @Nonnull Object lockKey) {
        return locks.get(LockKey.from(scope, lockKey));
    }
}
