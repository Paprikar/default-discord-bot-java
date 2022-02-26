package dev.paprikar.defaultdiscordbot.core.persistence.discord.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository of categories.
 */
@Repository
public interface DiscordCategoryRepository extends JpaRepository<DiscordCategory, Long> {

    /**
     * Finds all categories by the id of the guilds they are attached to.
     *
     * @param id
     *         the id of the guild
     *
     * @return the {@link List} of found categories
     */
    List<DiscordCategory> findAllByGuildId(Long id);

    /**
     * Finds all categories by the discord id of the guilds they are attached to.
     *
     * @param id
     *         the discord id of the guild
     *
     * @return the {@link List} of found categories
     */
    List<DiscordCategory> findAllByGuildDiscordId(Long id);
}
