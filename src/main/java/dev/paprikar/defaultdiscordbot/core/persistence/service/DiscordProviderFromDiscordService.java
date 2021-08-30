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

    @Nonnull
    public List<DiscordProviderFromDiscord> findAllProviders() {
        return repository.findAll();
    }

    @Nonnull
    public Optional<DiscordProviderFromDiscord> findProviderById(long id) {
        return repository.findById(id);
    }

    @Nonnull
    public DiscordProviderFromDiscord getProviderById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    @Nonnull
    public List<DiscordProviderFromDiscord> findProvidersByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    @Nonnull
    public List<DiscordProviderFromDiscord> findProvidersByCategoryGuildId(long id) {
        return repository.findAllByCategoryGuildId(id);
    }

    @Nonnull
    public List<DiscordProviderFromDiscord> findProvidersByCategoryGuildDiscordId(long id) {
        return repository.findAllByCategoryGuildDiscordId(id);
    }

    @Nonnull
    public DiscordProviderFromDiscord saveProvider(@Nonnull DiscordProviderFromDiscord category) {
        return repository.save(category);
    }

    public void deleteProvider(@Nonnull DiscordProviderFromDiscord category) {
        repository.delete(category);
    }

    public void deleteProviderById(long id) {
        repository.deleteById(id);
    }

    public void deleteAllProvidersByCategoryId(long id) {
        repository.deleteAllByCategoryId(id);
    }

    public void deleteAllProvidersByCategoryGuildId(long id) {
        repository.deleteAllByCategoryGuildId(id);
    }

    public void deleteAllProvidersByCategoryGuildDiscordId(long id) {
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
