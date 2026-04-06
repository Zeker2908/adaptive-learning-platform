package ru.zeker.task.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.zeker.common.dto.task.Difficulty;
import ru.zeker.common.dto.task.request.TaskRequest;
import ru.zeker.task.domain.mapper.TaskMapper;
import ru.zeker.task.domain.model.entity.Task;
import ru.zeker.task.exception.TaskNotFoundException;
import ru.zeker.task.repository.TaskRepository;
import ru.zeker.task.repository.specification.TaskSpecification;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final TagService tagService;
    private final TaskMapper taskMapper;

    public Page<Task> getTasks(String title,
                               List<Difficulty> difficulties,
                               List<String> tags,
                               int count) {

        Pageable pageable = Pageable.ofSize(count);
        return getTasksInternal(title, difficulties, tags, pageable);
    }

    public Page<Task> getTasksPaged(String title,
                                    List<Difficulty> difficulties,
                                    List<String> tags,
                                    Pageable pageable) {

        return getTasksInternal(title, difficulties, tags, pageable);
    }

    private Page<Task> getTasksInternal(String title,
                                        List<Difficulty> difficulties,
                                        List<String> tags,
                                        Pageable pageable) {

        log.debug("Find task with parameters title={}, diffList={}, tags={}, pageable={}",
                title, difficulties, tags, pageable);

        var spec = TaskSpecification.hasTitle(title)
                .and(TaskSpecification.hasDifficulties(difficulties))
                .and(TaskSpecification.hasAnyTags(tags));

        return repository.findAll(spec, pageable);
    }

    public Task getTask(UUID id) {
        log.debug("Find task with id {}", id);
        return repository.findById(id)
                .orElseThrow(TaskNotFoundException::new);
    }

    public List<Task> getRandomTasks(int count) {
        log.debug("Find random {} tasks", count);
        return repository.findRandomTasks(PageRequest.of(0, count))
                .stream()
                .toList();
    }

    @Transactional
    public Task createTask(TaskRequest request) {
        log.debug("Create task");
        var tagEntities = tagService.findOrCreateTags(request.getTags());
        var task = taskMapper.toEntity(request, tagEntities);
        return repository.save(task);
    }

    @Transactional
    public Task updateTask(UUID id, TaskRequest request) {
        log.debug("Update task with id {}", id);

        var task = repository.findById(id)
                .orElseThrow(TaskNotFoundException::new);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDifficulty(request.getDifficulty());

        var tagEntities = tagService.findOrCreateTags(request.getTags());
        task.setTags(tagEntities);

        return repository.save(task);
    }

    @Transactional
    public void deleteTask(UUID id) {
        log.debug("Delete task with id {}", id);

        if (!repository.existsById(id)) {
            throw new TaskNotFoundException();
        }

        repository.deleteById(id);
    }

    public boolean hasAnyTasks() {
        return repository.count() > 0;
    }

}
