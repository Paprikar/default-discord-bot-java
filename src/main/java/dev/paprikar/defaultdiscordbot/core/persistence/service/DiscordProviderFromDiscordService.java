package dev.paprikar.defaultdiscordbot.core.persistence.service;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import dev.paprikar.defaultdiscordbot.core.persistence.repository.DiscordProviderFromDiscordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class DiscordProviderFromDiscordService {

    private final DiscordProviderFromDiscordRepository repository;

    @Autowired
    public DiscordProviderFromDiscordService(DiscordProviderFromDiscordRepository repository) {
        this.repository = repository;
    }

    public List<DiscordProviderFromDiscord> findAll() {
        return repository.findAll();
    }

    public Optional<DiscordProviderFromDiscord> findById(long id) {
        return repository.findById(id);
    }

    public DiscordProviderFromDiscord getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    public List<DiscordProviderFromDiscord> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    public List<DiscordProviderFromDiscord> findAllByCategoryGuildId(long id) {
        return repository.findAllByCategoryGuildId(id);
    }

    public List<DiscordProviderFromDiscord> findAllByCategoryGuildDiscordId(long id) {
        return repository.findAllByCategoryGuildDiscordId(id);
    }

    public DiscordProviderFromDiscord save(@Nonnull DiscordProviderFromDiscord category) {
        return repository.save(category);
    }

    public void delete(@Nonnull DiscordProviderFromDiscord category) {
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

    public DiscordProviderFromDiscord attach(@Nonnull DiscordProviderFromDiscord provider,
                                             @Nonnull DiscordCategory category) {
        provider.attach(category);
        return repository.save(provider);
    }

    public void detach(@Nonnull DiscordProviderFromDiscord provider) {
        provider.detach();
        repository.delete(provider);
    }
}
