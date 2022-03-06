package dev.paprikar.defaultdiscordbot.core.persistence.discord.mediarequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The repository of media requests.
 */
@Repository
public interface DiscordMediaRequestRepository extends JpaRepository<DiscordMediaRequest, Long> {

    /**
     * Finds the oldest media request by its category id.
     *
     * @param id
     *         the category id of the media request
     *
     * @return the found media request
     */
    Optional<DiscordMediaRequest> findFirstByCategoryIdOrderByCreationTimestampAsc(Long id);

    /**
     * Counts all media requests with the specified id of their category.
     *
     * @param id
     *         the category id of the media requests
     *
     * @return the number of found media requests
     */
    long countByCategoryId(Long id);

    /**
     * Finds all media requests by the id of the category they are attached to.
     *
     * @param id
     *         the id of the category
     *
     * @return the {@link List} of found media requests
     */
    List<DiscordMediaRequest> findAllByCategoryId(Long id);

    /**
     * Deletes all media requests by the id of the category they are attached to.
     *
     * @param id
     *         the id of the category
     */
    void deleteByCategoryId(Long id);
}
