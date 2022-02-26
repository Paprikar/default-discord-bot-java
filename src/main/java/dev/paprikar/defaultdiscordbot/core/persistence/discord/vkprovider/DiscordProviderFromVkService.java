package dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.category.DiscordCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing vk providers.
 */
@Service
public class DiscordProviderFromVkService {

    private final DiscordProviderFromVkRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository
     *         an instance of {@link DiscordProviderFromVkRepository}
     */
    @Autowired
    public DiscordProviderFromVkService(DiscordProviderFromVkRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds all vk providers.
     *
     * @return the {@link List} with all found vk providers
     *
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    public List<DiscordProviderFromVk> findAll() {
        return repository.findAll();
    }

    /**
     * Finds the vk provider by its id.
     *
     * @param id
     *         the id of the vk provider
     *
     * @return the found vk provider
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository.findById()
     */
    public Optional<DiscordProviderFromVk> findById(long id) {
        return repository.findById(id);
    }

    /**
     * Returns the vk provider by its id.
     *
     * @param id
     *         the id of the vk provider
     *
     * @return the found vk provider
     *
     * @throws EntityNotFoundException
     *         see {@link EntityManager#getReference(Class, Object) EntityManager#getReference()} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById()
     * @see EntityManager#getReference(Class, Object) EntityManager#getReference()
     */
    public DiscordProviderFromVk getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Finds the vk providers by their category id.
     *
     * @param id
     *         the category id of the vk providers
     *
     * @return the {@link List} with all found vk providers
     */
    public List<DiscordProviderFromVk> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    /**
     * Saves the vk provider.
     *
     * @param provider
     *         the vk provider
     *
     * @return the saved vk provider
     */
    public DiscordProviderFromVk save(@Nonnull DiscordProviderFromVk provider) {
        return repository.save(provider);
    }

    /**
     * Deletes the vk provider.
     *
     * @param provider
     *         the vk provider
     */
    public void delete(@Nonnull DiscordProviderFromVk provider) {
        repository.delete(provider);
    }

    /**
     * Deletes all vk providers by their category id.
     *
     * @param id
     *         the category id of the vk providers
     */
    public void deleteByCategoryId(long id) {
        repository.deleteByCategoryId(id);
    }

    /**
     * Attaches the vk provider to the specified category and saves it.
     *
     * @param provider
     *         the vk provider
     * @param category
     *         the category
     *
     * @return the saved vk provider with all its changes
     */
    public DiscordProviderFromVk attach(@Nonnull DiscordProviderFromVk provider, @Nonnull DiscordCategory category) {
        provider.attach(category);
        return repository.save(provider);
    }

    /**
     * Detaches the vk provider from its category and deletes it.
     *
     * @param provider
     *         the vk provider
     */
    public void detach(@Nonnull DiscordProviderFromVk provider) {
        provider.detach();
        repository.delete(provider);
    }
}
