package dev.paprikar.defaultdiscordbot.core.persistence.repository;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordCategoryRepository extends JpaRepository<DiscordCategory, Long> {

    List<DiscordCategory> findAllByGuildId(Long id);

    List<DiscordCategory> findAllByGuildDiscordId(Long id);

    void deleteAllByGuildId(Long id);

    void deleteAllByGuildDiscordId(Long id);

    boolean existsByGuildId(Long id);

    boolean existsByGuildDiscordId(Long id);
}
