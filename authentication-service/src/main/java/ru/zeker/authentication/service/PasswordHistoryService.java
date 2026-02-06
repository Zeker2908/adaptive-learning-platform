package ru.zeker.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.zeker.authentication.domain.model.entity.PasswordHistory;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.exception.LocalAuthUserNotFoundException;
import ru.zeker.authentication.exception.PasswordHistoryException;
import ru.zeker.authentication.repository.PasswordHistoryRepository;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordHistoryService {
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.password-history.max-count:5}")
    private int maxPasswordHistoryCount;

    public Set<PasswordHistory> findAllByUserId(UUID userId) {
        return passwordHistoryRepository.findAllByLocalAuthId(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(User user, String rawPassword) {
        if (Objects.isNull(user.getLocalAuth())) {
            throw new LocalAuthUserNotFoundException("LocalAuth not found for user");
        }
        var existingPasswords = findAllByUserId(user.getId());

        var isPasswordReused = existingPasswords.parallelStream()
                .anyMatch(history -> passwordEncoder.matches(rawPassword, history.getPassword()));

        if (isPasswordReused) {
            throw new PasswordHistoryException("This password has already been used. Please choose a different password");
        }

        var passwordHistory = PasswordHistory.builder()
                .localAuth(user.getLocalAuth())
                .password(passwordEncoder.encode(rawPassword))
                .build();

        passwordHistoryRepository.save(passwordHistory);

        // Limiting the number of stored passwords
        var size = existingPasswords.size();
        if (size >= maxPasswordHistoryCount) {
            passwordHistoryRepository.deleteOldestByLocalAuthId(user.getId(), size - maxPasswordHistoryCount + 1);
        }
    }
}
