package dev.paprikar.defaultdiscordbot.core.persistence.discord.discordprovider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository of discord providers.
 */
@Repository
public interface DiscordProviderFromDiscordRepository extends JpaRepository<DiscordProviderFromDiscord, Long> {

    /**
     * Finds all discord providers by the id of the category they are attached to.
     *
     * @param id the id of the category
     *
     * @return the {@link List} of found discord providers
     */
    List<DiscordProviderFromDiscord> findAllByCategoryId(Long id);

    /**
     * Deletes all discord providers by the id of the category they are attached to.
     *
     * @param id the id of the category
     */
    void deleteByCategoryId(Long id);
}
