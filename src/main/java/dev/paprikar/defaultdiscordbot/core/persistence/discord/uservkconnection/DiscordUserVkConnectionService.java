package dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnection.ProjectionDiscordUserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing vk connections of discord users.
 */
@Service
public class DiscordUserVkConnectionService {

    private final DiscordUserVkConnectionRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository
     *         an instance of {@link DiscordUserVkConnectionRepository}
     */
    @Autowired
    public DiscordUserVkConnectionService(DiscordUserVkConnectionRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds the connection by its id.
     *
     * @param id
     *         the id of the connection
     *
     * @return the found connection
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository#findById(Object)
     */
    public Optional<DiscordUserVkConnection> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Returns the connection by its id.
     *
     * @param id
     *         the id of the connection
     *
     * @return the found connection
     *
     * @throws EntityNotFoundException
     *         see {@link EntityManager#getReference(Class, Object)} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById(Object)
     * @see EntityManager#getReference(Class, Object)
     */
    public DiscordUserVkConnection getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Does the connection exist with the specified discord user id?
     *
     * @param id
     *         the discord user id
     *
     * @return {@code true} if connection exists, otherwise {@code false}
     */
    public Boolean existsById(@Nonnull Long id) {
        return repository.existsById(id);
    }

    /**
     * Finds all connections by the vk user id.
     *
     * @param id
     *         the vk user id
     *
     * @return the {@link List} of entity projections of type {@link ProjectionDiscordUserId}
     */
    public List<ProjectionDiscordUserId> findAllByVkUserId(Integer id) {
        return repository.findAllByVkUserId(id);
    }

    /**
     * Saves the connection.
     *
     * @param connection
     *         the connection
     *
     * @return the saved connection
     */
    public DiscordUserVkConnection save(@Nonnull DiscordUserVkConnection connection) {
        return repository.save(connection);
    }

    /**
     * Deletes the connection.
     *
     * @param connection
     *         the connection
     */
    public void delete(@Nonnull DiscordUserVkConnection connection) {
        repository.delete(connection);
    }
}
