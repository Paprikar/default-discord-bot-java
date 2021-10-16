package dev.paprikar.defaultdiscordbot.core.persistence.service;

import dev.paprikar.defaultdiscordbot.core.persistence.entity.DiscordMediaRequest;
import dev.paprikar.defaultdiscordbot.core.persistence.repository.DiscordMediaRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class DiscordMediaRequestService {

    private final DiscordMediaRequestRepository repository;

    @Autowired
    public DiscordMediaRequestService(DiscordMediaRequestRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    public List<DiscordMediaRequest> findAll() {
        return repository.findAll();
    }

    @Nonnull
    public Optional<DiscordMediaRequest> findById(long id) {
        return repository.findById(id);
    }

    @Nonnull
    public DiscordMediaRequest getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    @Nonnull
    public List<DiscordMediaRequest> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    @Nonnull
    public DiscordMediaRequest save(@Nonnull DiscordMediaRequest mediaRequest) {
        return repository.save(mediaRequest);
    }

    public void delete(@Nonnull DiscordMediaRequest mediaRequest) {
        repository.delete(mediaRequest);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    @Nonnull
    public Optional<DiscordMediaRequest> findFirstByCategoryId(long id) {
        return repository.findFirstByCategoryIdOrderByCreationDateTimeAsc(id);
    }

    @Nonnull
    public DiscordMediaRequest getFirstByCategoryId(long id) throws EntityNotFoundException {
        return repository.getFirstByCategoryIdOrderByCreationDateTimeAsc(id);
    }

    @Nonnull
    public long countByCategoryId(long id) {
        return repository.countByCategoryId(id);
    }
}
