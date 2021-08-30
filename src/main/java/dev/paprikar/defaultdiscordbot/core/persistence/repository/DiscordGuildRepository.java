package dev.paprikar.defaultdiscordbot.core.persistence.repository;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordGuild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordGuildRepository extends JpaRepository<DiscordGuild, Long> {

    Optional<DiscordGuild> findByDiscordId(Long id);

    DiscordGuild getByDiscordId(Long id);

    void deleteByDiscordId(Long id);

    boolean existsByDiscordId(Long id);
}
