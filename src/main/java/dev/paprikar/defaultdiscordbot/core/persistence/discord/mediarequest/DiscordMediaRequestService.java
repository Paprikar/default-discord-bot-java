package dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing categories media requests.
 */
@Service
public class DiscordMediaRequestService {

    private final DiscordMediaRequestRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository
     *         an instance of {@link DiscordMediaRequestRepository}
     */
    @Autowired
    public DiscordMediaRequestService(DiscordMediaRequestRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds all media requests.
     *
     * @return the {@link List} with all found media requests
     *
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    public List<DiscordMediaRequest> findAll() {
        return repository.findAll();
    }

    /**
     * Finds the media request by its id.
     *
     * @param id
     *         the id of the media request
     *
     * @return the found media request
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository.findById()
     */
    public Optional<DiscordMediaRequest> findById(long id) {
        return repository.findById(id);
    }

    /**
     * Returns the media request by its id.
     *
     * @param id
     *         the id of the media request
     *
     * @return the found media request
     *
     * @throws EntityNotFoundException
     *         see {@link EntityManager#getReference(Class, Object) EntityManager#getReference()} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById()
     * @see EntityManager#getReference(Class, Object) EntityManager#getReference()
     */
    public DiscordMediaRequest getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Finds the media requests by their category id.
     *
     * @param id
     *         the category id of the media requests
     *
     * @return the {@link List} with all found media requests
     */
    public List<DiscordMediaRequest> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    /**
     * Saves the media request.
     *
     * @param mediaRequest
     *         the media request
     *
     * @return the saved media request
     */
    public DiscordMediaRequest save(@Nonnull DiscordMediaRequest mediaRequest) {
        return repository.save(mediaRequest);
    }

    /**
     * Deletes the media request.
     *
     * @param mediaRequest
     *         the media request
     */
    public void delete(@Nonnull DiscordMediaRequest mediaRequest) {
        repository.delete(mediaRequest);
    }

    /**
     * Deletes all media requests by their category id.
     *
     * @param id
     *         the category id of the media requests
     */
    public void deleteByCategoryId(long id) {
        repository.deleteByCategoryId(id);
    }

    /**
     * Finds the oldest media request by its category id.
     *
     * @param id
     *         the category id of the media request
     *
     * @return the found media request
     */
    public Optional<DiscordMediaRequest> findFirstByCategoryId(long id) {
        return repository.findFirstByCategoryIdOrderByCreationTimestampAsc(id);
    }

    /**
     * Counts all media requests with the specified id of their category.
     *
     * @param id
     *         the category id of the media requests
     *
     * @return the number of found media requests
     */
    public long countByCategoryId(long id) {
        return repository.countByCategoryId(id);
    }
}
