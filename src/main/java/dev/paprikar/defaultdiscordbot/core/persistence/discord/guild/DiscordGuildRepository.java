package dev.paprikar.defaultdiscordbot.core.persistence.discord.guild;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The repository of guilds.
 */
@Repository
public interface DiscordGuildRepository extends JpaRepository<DiscordGuild, Long> {

    /**
     * Finds the guild by its discord id.
     *
     * @param id the discord id of the guild
     *
     * @return the found guild
     */
    Optional<DiscordGuild> findByDiscordId(Long id);

    /**
     * Deletes the guild by its discord id.
     *
     * @param id the discord id of the guild
     */
    void deleteByDiscordId(Long id);
}
