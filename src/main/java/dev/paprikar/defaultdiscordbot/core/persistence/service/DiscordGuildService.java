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

    public List<DiscordGuild> findAll() {
        return repository.findAll();
    }

    public Optional<DiscordGuild> findById(long id) {
        return repository.findById(id);
    }

    public DiscordGuild getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    public Optional<DiscordGuild> findByDiscordId(long id) {
        return repository.findByDiscordId(id);
    }

    public DiscordGuild getByDiscordId(long id) throws EntityNotFoundException {
        return repository.getByDiscordId(id);
    }

    public DiscordGuild save(@Nonnull DiscordGuild guild) {
        return repository.save(guild);
    }

    public void delete(@Nonnull DiscordGuild guild) {
        repository.delete(guild);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public void deleteByDiscordId(long id) {
        repository.deleteByDiscordId(id);
    }

    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    public boolean existsByDiscordId(long id) {
        return repository.existsByDiscordId(id);
    }
}
