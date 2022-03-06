package dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing trusted suggesters.
 */
@Service
public class DiscordTrustedSuggesterService {

    private final DiscordTrustedSuggesterRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository
     *         an instance of {@link DiscordTrustedSuggesterRepository}
     */
    @Autowired
    public DiscordTrustedSuggesterService(DiscordTrustedSuggesterRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds the suggester by its id.
     *
     * @param id
     *         the id of the suggester
     *
     * @return the found suggester
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository#findById(Object)
     */
    public Optional<DiscordTrustedSuggester> findById(long id) {
        return repository.findById(id);
    }

    /**
     * Finds the suggester by its category id and user id.
     *
     * @param categoryId
     *         the category id
     * @param userId
     *         the user id
     *
     * @return the found suggester
     */
    public Optional<DiscordTrustedSuggester> findByCategoryIdAndUserId(long categoryId, long userId) {
        return repository.findByCategoryIdAndUserId(categoryId, userId);
    }

    /**
     * Returns the suggester by its id.
     *
     * @param id
     *         the id of the suggester
     *
     * @return the found suggester
     *
     * @throws EntityNotFoundException
     *         see {@link EntityManager#getReference(Class, Object)} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById(Object)
     * @see EntityManager#getReference(Class, Object)
     */
    public DiscordTrustedSuggester getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Does the suggester exist with the specified category id and user id?
     *
     * @param categoryId
     *         the category id
     * @param userId
     *         the user id
     *
     * @return {@code true} if suggester exists, otherwise {@code false}
     */
    public Boolean existsByCategoryIdAndUserId(long categoryId, long userId) {
        return repository.existsByCategoryIdAndUserId(categoryId, userId);
    }

    /**
     * Does the suggester exist with the specified category id and any of user ids?
     *
     * @param categoryId
     *         the category id
     * @param userIds
     *         a {@link List} of user ids
     *
     * @return {@code true} if suggester exists, otherwise {@code false}
     */
    public Boolean existsByCategoryIdAndUserIdIn(long categoryId, @Nonnull List<Long> userIds) {
        return repository.existsByCategoryIdAndUserIdIn(categoryId, userIds);
    }

    /**
     * Finds all suggesters.
     *
     * @return the {@link List} with all found suggesters
     *
     * @see org.springframework.data.repository.CrudRepository#findAll() CrudRepository#findAll()
     */
    public List<DiscordTrustedSuggester> findAll() {
        return repository.findAll();
    }

    /**
     * Finds all suggesters by their category id.
     *
     * @param id
     *         the category id
     *
     * @return the {@link List} of found suggesters
     */
    public List<DiscordTrustedSuggester> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    /**
     * Saves the suggester.
     *
     * @param suggester
     *         the suggester
     *
     * @return the saved suggester
     */
    public DiscordTrustedSuggester save(@Nonnull DiscordTrustedSuggester suggester) {
        return repository.save(suggester);
    }

    /**
     * Deletes the suggester.
     *
     * @param suggester
     *         the suggester
     */
    public void delete(@Nonnull DiscordTrustedSuggester suggester) {
        repository.delete(suggester);
    }

    /**
     * Deletes all suggesters by their category id.
     *
     * @param id
     *         the category id
     */
    public void deleteByCategoryId(long id) {
        repository.deleteByCategoryId(id);
    }
}
