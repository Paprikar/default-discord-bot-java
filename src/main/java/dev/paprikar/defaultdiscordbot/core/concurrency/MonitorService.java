package dev.paprikar.defaultdiscordbot.core.concurrency;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for working with monitor objects for concurrency.
 * <p>
 * The service is thread safe.
 */
@Service
public class MonitorService {

    // Map<ConcurrencyKey, Monitor>
    private final Map<ConcurrencyKey, Object> locks = new ConcurrentHashMap<>();

    /**
     * Returns the monitor object that is associated with the corresponding concurrency key.
     *
     * @param key
     *         the concurrency key whose associated monitor object is to be returned
     *
     * @return the monitor object that is associated with the specified
     * concurrency key, or {@code null} if there is no association
     */
    public Object get(@Nonnull ConcurrencyKey key) {
        return locks.get(key);
    }

    /**
     * Returns the monitor object that is associated with the corresponding concurrency scope and object.
     *
     * @param scope
     *         the concurrency scope to use with an object to return the associated monitor object
     * @param key
     *         the object to use with a concurrency scope to return the associated monitor object
     *
     * @return the monitor object that is associated with the specified
     * concurrency scope and object, or {@code null} if there is no association
     */
    public Object get(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return get(ConcurrencyKey.from(scope, key));
    }

    /**
     * Constructs and returns a new monitor object.
     *
     * @param key
     *         the concurrency key whose associated monitor object is to be created and returned
     *
     * @return the new monitor object that is associated with the specified concurrency key
     */
    public Object add(@Nonnull ConcurrencyKey key) {
        return locks.put(key, new Object());
    }

    /**
     * Constructs and returns a new monitor object.
     *
     * @param scope
     *         the concurrency scope to use with an object to create and return the associated monitor object
     * @param key
     *         the object to use with a concurrency scope to create and return the associated monitor object
     *
     * @return the new monitor object that is associated with the specified concurrency scope and object
     */
    public Object add(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return add(ConcurrencyKey.from(scope, key));
    }

    /**
     * If the specified concurrency key is not already associated with a monitor object associates it
     * with the given monitor object and returns {@code null}, else returns the current monitor object.
     *
     * @param key
     *         the concurrency key with which the specified monitor object will be associated
     * @param monitor
     *         monitor object to be associated with the specified concurrency key
     *
     * @return the previous monitor object associated with the specified concurrency
     * key, or {@code null} if there was no association for the concurrency key
     *
     * @see java.util.Map#putIfAbsent(Object, Object)
     */
    public Object putIfAbsent(@Nonnull ConcurrencyKey key, @Nonnull Object monitor) {
        return locks.putIfAbsent(key, monitor);
    }

    /**
     * If the specified concurrency scope and object is not already associated with a monitor object associates
     * it with the given monitor object and returns {@code null}, else returns the current monitor object.
     *
     * @param scope
     *         the concurrency scope to use with an object with which the specified monitor object will be associated
     * @param key
     *         the object to use with a concurrency scope with which the specified monitor object will be associated
     * @param monitor
     *         monitor object to be associated with the specified concurrency scope and object
     *
     * @return the previous monitor object associated with the specified concurrency scope and
     * object, or {@code null} if there was no association for the concurrency scope and object
     *
     * @see java.util.Map#putIfAbsent(Object, Object)
     */
    public Object putIfAbsent(@Nonnull ConcurrencyScope scope, @Nonnull Object key, @Nonnull Object monitor) {
        return putIfAbsent(ConcurrencyKey.from(scope, key), monitor);
    }

    /**
     * Removes and returns the monitor object.
     *
     * @param key
     *         the concurrency key whose associated monitor object is to be removed and returned
     *
     * @return the monitor object that was associated with the specified concurrency key
     */
    public Object remove(@Nonnull ConcurrencyKey key) {
        return locks.remove(key);
    }

    /**
     * Removes and returns the monitor object.
     *
     * @param scope
     *         the concurrency scope to use with an object to remove and return the associated monitor object
     * @param key
     *         the object to use with a concurrency scope to remove and return the associated monitor object
     *
     * @return the monitor object that was associated with the specified concurrency scope and object
     */
    public Object remove(@Nonnull ConcurrencyScope scope, @Nonnull Object key) {
        return remove(ConcurrencyKey.from(scope, key));
    }
}
