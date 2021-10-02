package dev.paprikar.defaultdiscordbot.core.persistence.repository;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscordMediaRequestRepository extends JpaRepository<DiscordMediaRequest, Long> {

    List<DiscordMediaRequest> findAllByCategoryId(Long id);

    Optional<DiscordMediaRequest> findFirstByCategoryIdOrderByCreationDateTimeAsc(Long id);

    DiscordMediaRequest getFirstByCategoryIdOrderByCreationDateTimeAsc(Long id);

    long countByCategoryId(Long id);
}
