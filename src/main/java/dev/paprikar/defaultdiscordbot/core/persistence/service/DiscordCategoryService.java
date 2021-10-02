package dev.paprikar.defaultdiscordbot.core.persistence.service;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import dev.paprikar.defaultdiscordbot.core.persistence.repository.DiscordCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class DiscordCategoryService {

    private final DiscordCategoryRepository repository;

    @Autowired
    public DiscordCategoryService(DiscordCategoryRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    public List<DiscordCategory> findAll() {
        return repository.findAll();
    }

    @Nonnull
    public Optional<DiscordCategory> findById(long id) {
        return repository.findById(id);
    }

    @Nonnull
    public DiscordCategory getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    @Nonnull
    public List<DiscordCategory> findAllByGuildId(long id) {
        return repository.findAllByGuildId(id);
    }

    @Nonnull
    public List<DiscordCategory> findAllByGuildDiscordId(long id) {
        return repository.findAllByGuildDiscordId(id);
    }

    @Nonnull
    public DiscordCategory save(@Nonnull DiscordCategory category) {
        return repository.save(category);
    }

    public void delete(@Nonnull DiscordCategory category) {
        repository.delete(category);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public void deleteAllByGuildId(long id) {
        repository.deleteAllByGuildId(id);
    }

    public void deleteAllByGuildDiscordId(long id) {
        repository.deleteAllByGuildDiscordId(id);
    }

    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    public boolean existsByGuildId(long id) {
        return repository.existsByGuildId(id);
    }

    public boolean existsByGuildDiscordId(long id) {
        return repository.existsByGuildDiscordId(id);
    }

    @Nonnull
    public DiscordCategory attach(@Nonnull DiscordCategory category, @Nonnull DiscordGuild guild) {
        category.attach(guild);
        return repository.save(category);
    }

    public void detach(@Nonnull DiscordCategory category) {
        category.detach();
        repository.delete(category);
    }
}
