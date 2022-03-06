package dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection;

import dev.paprikar.defaultdiscordbot.core.persistence.discord.uservkconnection.DiscordUserVkConnection.ProjectionDiscordUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository of vk connections of discord users.
 */
@Repository
public interface DiscordUserVkConnectionRepository extends JpaRepository<DiscordUserVkConnection, Long> {

    /**
     * Finds all connections by the vk user id.
     *
     * @param id
     *         the vk user id
     *
     * @return the {@link List} of entity projections of type {@link ProjectionDiscordUserId}
     */
    List<ProjectionDiscordUserId> findAllByVkUserId(Integer id);
}
