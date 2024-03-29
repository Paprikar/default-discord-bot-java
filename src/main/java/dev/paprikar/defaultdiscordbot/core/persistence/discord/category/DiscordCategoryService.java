package dev.paprikar.defaultdiscordbot.core.persistence.discord.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing categories.
 */
@Service
public class DiscordCategoryService {

    private final DiscordCategoryRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository
     *         an instance of {@link DiscordCategoryRepository}
     */
    @Autowired
    public DiscordCategoryService(DiscordCategoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds the category by its id.
     *
     * @param id
     *         the id of the category
     *
     * @return the found category
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository#findById(Object)
     */
    public Optional<DiscordCategory> findById(long id) {
        return repository.findById(id);
    }

    /**
     * Returns the category by its id.
     *
     * @param id
     *         the id of the category
     *
     * @return the found category
     *
     * @throws EntityNotFoundException
     *         see {@link EntityManager#getReference(Class, Object)} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById(Object)
     * @see EntityManager#getReference(Class, Object)
     */
    public DiscordCategory getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Finds all categories.
     *
     * @return the {@link List} with all found categories
     *
     * @see org.springframework.data.repository.CrudRepository#findAll() CrudRepository#findAll()
     */
    public List<DiscordCategory> findAll() {
        return repository.findAll();
    }

    /**
     * Finds the categories by their guild id.
     *
     * @param id
     *         the guild id of the categories
     *
     * @return the {@link List} with all found categories
     */
    public List<DiscordCategory> findAllByGuildId(long id) {
        return repository.findAllByGuildId(id);
    }

    /**
     * Finds the categories by their discord id of the guild.
     *
     * @param id
     *         the guild discord id of the categories
     *
     * @return the {@link List} with all found categories
     */
    public List<DiscordCategory> findAllByGuildDiscordId(long id) {
        return repository.findAllByGuildDiscordId(id);
    }

    /**
     * Saves the category.
     *
     * @param category
     *         the category
     *
     * @return the saved category
     */
    public DiscordCategory save(@Nonnull DiscordCategory category) {
        return repository.save(category);
    }

    /**
     * Deletes the category.
     *
     * @param category
     *         the category
     */
    public void delete(@Nonnull DiscordCategory category) {
        repository.delete(category);
    }

    /**
     * Deletes the category by its id.
     *
     * @param id
     *         the id of the category
     */
    public void deleteById(long id) {
        repository.deleteById(id);
    }
}
