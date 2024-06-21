package dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing discord providers.
 */
@Service
public class DiscordProviderFromDiscordService {

    private final DiscordProviderFromDiscordRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository an instance of {@link DiscordProviderFromDiscordRepository}
     */
    @Autowired
    public DiscordProviderFromDiscordService(DiscordProviderFromDiscordRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds the discord provider by its id.
     *
     * @param id the id of the discord provider
     *
     * @return the found discord provider
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository#findById(Object)
     */
    public Optional<DiscordProviderFromDiscord> findById(long id) {
        return repository.findById(id);
    }

    /**
     * Returns the discord provider by its id.
     *
     * @param id the id of the discord provider
     *
     * @return the found discord provider
     *
     * @throws EntityNotFoundException see {@link EntityManager#getReference(Class, Object)} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById(Object)
     * @see EntityManager#getReference(Class, Object)
     */
    public DiscordProviderFromDiscord getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Finds all discord providers.
     *
     * @return the {@link List} with all found discord providers
     *
     * @see org.springframework.data.repository.CrudRepository#findAll() CrudRepository#findAll()
     */
    public List<DiscordProviderFromDiscord> findAll() {
        return repository.findAll();
    }

    /**
     * Finds the discord providers by their category id.
     *
     * @param id the category id of the discord providers
     *
     * @return the {@link List} with all found discord providers
     */
    public List<DiscordProviderFromDiscord> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    /**
     * Saves the discord provider.
     *
     * @param provider the discord provider
     *
     * @return the saved discord provider
     */
    public DiscordProviderFromDiscord save(@Nonnull DiscordProviderFromDiscord provider) {
        return repository.save(provider);
    }

    /**
     * Deletes the discord provider.
     *
     * @param provider the discord provider
     */
    public void delete(@Nonnull DiscordProviderFromDiscord provider) {
        repository.delete(provider);
    }

    /**
     * Deletes all discord providers by their category id.
     *
     * @param id the category id of the discord providers
     */
    public void deleteByCategoryId(long id) {
        repository.deleteByCategoryId(id);
    }
}
