package dev.paprikar.defaultdiscordbot.core.persistence.service;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.repository.DiscordGuildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class DiscordGuildService {

    private final DiscordGuildRepository repository;

    @Autowired
    public DiscordGuildService(DiscordGuildRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    public List<DiscordGuild> findAllGuilds() {
        return repository.findAll();
    }

    @Nonnull
    public Optional<DiscordGuild> findGuildById(long id) {
        return repository.findById(id);
    }

    @Nonnull
    public DiscordGuild getGuildById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    @Nonnull
    public Optional<DiscordGuild> findGuildByDiscordId(long id) {
        return repository.findByDiscordId(id);
    }

    @Nonnull
    public DiscordGuild getGuildByDiscordId(long id) throws EntityNotFoundException {
        return repository.getByDiscordId(id);
    }

    @Nonnull
    public DiscordGuild saveGuild(@Nonnull DiscordGuild guild) {
        return repository.save(guild);
    }

    public void deleteGuild(@Nonnull DiscordGuild guild) {
        repository.delete(guild);
    }

    public void deleteGuildById(long id) {
        repository.deleteById(id);
    }

    public void deleteGuildByDiscordId(long id) {
        repository.deleteByDiscordId(id);
    }

    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    public boolean existsByDiscordId(long id) {
        return repository.existsByDiscordId(id);
    }
}
