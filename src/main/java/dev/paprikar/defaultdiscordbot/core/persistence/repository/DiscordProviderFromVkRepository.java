package dev.paprikar.defaultdiscordbot.core.persistence.repository;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordProviderFromVk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordProviderFromVkRepository extends JpaRepository<DiscordProviderFromVk, Long> {

    List<DiscordProviderFromVk> findAllByCategoryId(Long id);

    List<DiscordProviderFromVk> findAllByCategoryGuildId(Long id);

    List<DiscordProviderFromVk> findAllByCategoryGuildDiscordId(Long id);

    void deleteAllByCategoryId(Long id);

    void deleteAllByCategoryGuildId(Long id);

    void deleteAllByCategoryGuildDiscordId(Long id);

    boolean existsByCategoryId(Long id);

    boolean existsByCategoryGuildId(Long id);

    boolean existsByCategoryGuildDiscordId(Long id);
}
