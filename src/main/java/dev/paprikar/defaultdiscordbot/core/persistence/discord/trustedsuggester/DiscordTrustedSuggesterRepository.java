package dev.paprikar.defaultdiscordbot.core.persistence.discord.trustedsuggester;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The repository of trusted suggesters.
 */
@Repository
public interface DiscordTrustedSuggesterRepository extends JpaRepository<DiscordTrustedSuggester, Long> {

    /**
     * Finds the suggester by its category id and user id.
     *
     * @param categoryId the category id
     * @param userId the user id
     *
     * @return the found suggester
     */
    Optional<DiscordTrustedSuggester> findByCategoryIdAndUserId(Long categoryId, Long userId);

    /**
     * Does the suggester exist with the specified category id and user id?
     *
     * @param categoryId the category id
     * @param userId the user id
     *
     * @return {@code true} if suggester exists, otherwise {@code false}
     */
    Boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    /**
     * Does the suggester exist with the specified category id and any of user ids?
     *
     * @param categoryId the category id
     * @param userIds a {@link List} of user ids
     *
     * @return {@code true} if suggester exists, otherwise {@code false}
     */
    Boolean existsByCategoryIdAndUserIdIn(Long categoryId, List<Long> userIds);

    /**
     * Finds all suggesters by their category id.
     *
     * @param id the category id
     *
     * @return the {@link List} of found suggesters
     */
    List<DiscordTrustedSuggester> findAllByCategoryId(Long id);

    /**
     * Deletes all suggesters by their category id.
     *
     * @param id the category id
     */
    void deleteByCategoryId(Long id);
}
