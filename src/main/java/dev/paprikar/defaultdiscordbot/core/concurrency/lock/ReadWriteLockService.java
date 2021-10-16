package dev.paprikar.defaultdiscordbot.core.concurrency.lock;

import dev.paprikar.defaultdiscordbot.core.persistence.service.DiscordCategoryService;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ReadWriteLockService {

    private final DiscordCategoryService categoryService;

    // Map<LockKey, GuildLockObject>
    private final Map<LockKey<ReadWriteLockScope>, ReadWriteLock> locks = new ConcurrentHashMap<>();

    public ReadWriteLockService(DiscordCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void add(@Nonnull LockKey<ReadWriteLockScope> key) {
        locks.put(key, new ReentrantReadWriteLock());
    }

    public void add(@Nonnull ReadWriteLockScope scope, @Nonnull Object lockKey) {
        locks.put(LockKey.from(scope, lockKey), new ReentrantReadWriteLock());
    }

    public void remove(@Nonnull LockKey<ReadWriteLockScope> key) {
        locks.remove(key);
    }

    public void remove(@Nonnull ReadWriteLockScope scope, @Nonnull Object lockKey) {
        locks.remove(LockKey.from(scope, lockKey));
    }

    public void handle(@Nonnull GuildJoinEvent event) {
        locks.put(LockKey.from(ReadWriteLockScope.GUILD_CONFIGURATION, event.getGuild().getIdLong()),
                new ReentrantReadWriteLock());
    }

    public void handle(@Nonnull UnavailableGuildJoinedEvent event) {
        locks.put(LockKey.from(ReadWriteLockScope.GUILD_CONFIGURATION, event.getGuildIdLong()),
                new ReentrantReadWriteLock());
    }

    public void handle(@Nonnull GuildLeaveEvent event) {
        locks.remove(LockKey.from(ReadWriteLockScope.GUILD_CONFIGURATION, event.getGuild().getIdLong()));
    }

    public void handle(@Nonnull UnavailableGuildLeaveEvent event) {
        locks.remove(LockKey.from(ReadWriteLockScope.GUILD_CONFIGURATION, event.getGuildIdLong()));
    }

    public ReadWriteLock get(@Nonnull LockKey<ReadWriteLockScope> key) {
        return locks.get(key);
    }

    public ReadWriteLock get(@Nonnull ReadWriteLockScope scope, @Nonnull Object lockKey) {
        return locks.get(LockKey.from(scope, lockKey));
    }

    public ReadWriteLock getByCategoryId(@Nonnull ReadWriteLockScope scope, long categoryId) {
        return categoryService
                .findById(categoryId)
                .map(category -> locks.get(LockKey.from(scope, category.getGuild().getId())))
                .orElse(null);
    }
}
