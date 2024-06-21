package dev.paprikar.defaultdiscordbot.core.persistence.discord.vkprovider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository of vk providers.
 */
@Repository
public interface DiscordProviderFromVkRepository extends JpaRepository<DiscordProviderFromVk, Long> {

    /**
     * Finds all vk providers by the id of the category they are attached to.
     *
     * @param id the id of the category
     *
     * @return the {@link List} of found vk providers
     */
    List<DiscordProviderFromVk> findAllByCategoryId(Long id);

    /**
     * Deletes all vk providers by the id of the category they are attached to.
     *
     * @param id the id of the category
     */
    void deleteByCategoryId(Long id);
}
