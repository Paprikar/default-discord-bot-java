package dev.paprikar.defaultdiscordbot.core.persistence.service;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import dev.paprikar.defaultdiscordbot.core.persistence.repository.DiscordProviderFromVkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class DiscordProviderFromVkService {

    private final DiscordProviderFromVkRepository repository;

    @Autowired
    public DiscordProviderFromVkService(DiscordProviderFromVkRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    public List<DiscordProviderFromVk> findAll() {
        return repository.findAll();
    }

    @Nonnull
    public Optional<DiscordProviderFromVk> findById(long id) {
        return repository.findById(id);
    }

    @Nonnull
    public DiscordProviderFromVk getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    @Nonnull
    public List<DiscordProviderFromVk> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    @Nonnull
    public List<DiscordProviderFromVk> findAllByCategoryGuildId(long id) {
        return repository.findAllByCategoryGuildId(id);
    }

    @Nonnull
    public List<DiscordProviderFromVk> findAllByCategoryGuildDiscordId(long id) {
        return repository.findAllByCategoryGuildDiscordId(id);
    }

    @Nonnull
    public DiscordProviderFromVk save(@Nonnull DiscordProviderFromVk category) {
        return repository.save(category);
    }

    public void delete(@Nonnull DiscordProviderFromVk category) {
        repository.delete(category);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public void deleteAllByCategoryId(long id) {
        repository.deleteAllByCategoryId(id);
    }

    public void deleteAllByCategoryGuildId(long id) {
        repository.deleteAllByCategoryGuildId(id);
    }

    public void deleteAllByCategoryGuildDiscordId(long id) {
        repository.deleteAllByCategoryGuildDiscordId(id);
    }

    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    public boolean existsByCategoryId(long id) {
        return repository.existsByCategoryId(id);
    }

    public boolean existsByCategoryGuildId(long id) {
        return repository.existsByCategoryGuildId(id);
    }

    public boolean existsByCategoryGuildDiscordId(long id) {
        return repository.existsByCategoryGuildDiscordId(id);
    }

    @Nonnull
    public DiscordProviderFromVk attach(@Nonnull DiscordProviderFromVk provider,
                                        @Nonnull DiscordCategory category) {
        provider.attach(category);
        return repository.save(provider);
    }

    public void detach(@Nonnull DiscordProviderFromVk provider) {
        provider.detach();
        repository.delete(provider);
    }
}
