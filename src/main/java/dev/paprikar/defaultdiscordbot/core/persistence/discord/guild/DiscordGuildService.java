package dev.paprikar.defaultdiscordbot.core.persistence.discord.guild;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing guilds.
 */
@Service
public class DiscordGuildService {

    private final DiscordGuildRepository repository;

    /**
     * Constructs the service.
     *
     * @param repository
     *         an instance of {@link DiscordGuildRepository}
     */
    @Autowired
    public DiscordGuildService(DiscordGuildRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds the guild by its id.
     *
     * @param id
     *         the id of the guild
     *
     * @return the found guild
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository#findById(Object)
     */
    public Optional<DiscordGuild> findById(long id) {
        return repository.findById(id);
    }

    /**
     * Finds the guild by its discord id.
     *
     * @param id
     *         the discord id of the guild
     *
     * @return the found guild
     *
     * @see org.springframework.data.repository.CrudRepository#findById(Object) CrudRepository#findById(Object)
     */
    public Optional<DiscordGuild> findByDiscordId(long id) {
        return repository.findByDiscordId(id);
    }

    /**
     * Returns the guild by its id.
     *
     * @param id
     *         the id of the guild
     *
     * @return the found guild
     *
     * @throws EntityNotFoundException
     *         see {@link EntityManager#getReference(Class, Object)} for more details
     * @see org.springframework.data.jpa.repository.JpaRepository#getById(Object) JpaRepository#getById(Object)
     * @see EntityManager#getReference(Class, Object)
     */
    public DiscordGuild getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    /**
     * Finds all guilds.
     *
     * @return the {@link List} with all found guilds
     *
     * @see org.springframework.data.repository.CrudRepository#findAll() CrudRepository#findAll()
     */
    public List<DiscordGuild> findAll() {
        return repository.findAll();
    }

    /**
     * Saves the guild.
     *
     * @param guild
     *         the guild
     *
     * @return the saved guild
     */
    public DiscordGuild save(@Nonnull DiscordGuild guild) {
        return repository.save(guild);
    }

    /**
     * Deletes the guild.
     *
     * @param guild
     *         the guild
     */
    public void delete(@Nonnull DiscordGuild guild) {
        repository.delete(guild);
    }

    /**
     * Deletes the guild by its discord id.
     *
     * @param id
     *         the discord id of the guild
     */
    public void deleteByDiscordId(long id) {
        repository.deleteByDiscordId(id);
    }
}
