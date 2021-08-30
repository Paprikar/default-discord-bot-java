package dev.paprikar.defaultdiscordbot.core.persistence.repository;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromDiscord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordProviderFromDiscordRepository extends JpaRepository<DiscordProviderFromDiscord, Long> {

    List<DiscordProviderFromDiscord> findAllByCategoryId(Long id);

    List<DiscordProviderFromDiscord> findAllByCategoryGuildId(Long id);

    List<DiscordProviderFromDiscord> findAllByCategoryGuildDiscordId(Long id);

    void deleteAllByCategoryId(Long id);

    void deleteAllByCategoryGuildId(Long id);

    void deleteAllByCategoryGuildDiscordId(Long id);

    boolean existsByCategoryId(Long id);

    boolean existsByCategoryGuildId(Long id);

    boolean existsByCategoryGuildDiscordId(Long id);
}
