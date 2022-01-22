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

    public List<DiscordMediaRequest> findAll() {
        return repository.findAll();
    }

    public Optional<DiscordMediaRequest> findById(long id) {
        return repository.findById(id);
    }

    public DiscordMediaRequest getById(long id) throws EntityNotFoundException {
        return repository.getById(id);
    }

    public List<DiscordMediaRequest> findAllByCategoryId(long id) {
        return repository.findAllByCategoryId(id);
    }

    public DiscordMediaRequest save(@Nonnull DiscordMediaRequest mediaRequest) {
        return repository.save(mediaRequest);
    }

    public void delete(@Nonnull DiscordMediaRequest mediaRequest) {
        repository.delete(mediaRequest);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public void deleteByCategoryId(long id) {
        repository.deleteByCategoryId(id);
    }

    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    public Optional<DiscordMediaRequest> findFirstByCategoryId(long id) {
        return repository.findFirstByCategoryIdOrderByCreationTimestampAsc(id);
    }

    public DiscordMediaRequest getFirstByCategoryId(long id) throws EntityNotFoundException {
        return repository.getFirstByCategoryIdOrderByCreationTimestampAsc(id);
    }

    public long countByCategoryId(long id) {
        return repository.countByCategoryId(id);
    }
}
