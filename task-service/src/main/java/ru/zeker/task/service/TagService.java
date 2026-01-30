package ru.zeker.task.service;

import ru.zeker.task.domain.model.entity.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.zeker.task.repository.TagRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository repository;

    @Transactional
    public Set<Tag> findOrCreateTags(Set<String> tags) {
        Optional.ofNullable(tags)
                .filter(t -> !t.isEmpty())
                .orElseThrow(() ->
                        new IllegalArgumentException("Tags is null or empty"));

        Set<Tag> existingTags = repository.findByNameIn(tags);
        Set<String> existingNames = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());

        Set<Tag> newTags = tags.stream()
                .filter(n -> !existingNames.contains(n))
                .map(this::buildTag)
                .collect(Collectors.toSet());

        if (!newTags.isEmpty()) {
            repository.saveAll(newTags);
        }

        return Stream.concat(
                existingTags.stream(),
                newTags.stream()
        ).collect(Collectors.toSet());

    }

    private Tag buildTag(String name) {
        return Tag.builder().name(name).build();
    }
}
