package ru.zeker.authentication.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.zeker.authentication.domain.dto.request.BindPasswordRequest;
import ru.zeker.authentication.domain.dto.request.UserUpdateRequest;
import ru.zeker.authentication.domain.model.entity.LocalAuth;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.domain.model.enums.Role;
import ru.zeker.authentication.exception.UserAlreadyExistsException;
import ru.zeker.authentication.exception.UserNotFoundException;
import ru.zeker.authentication.repository.UserRepository;
import ru.zeker.common.exception.ErrorCode;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordHistoryService passwordHistoryService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found", ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found", ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public User create(@NotNull User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists", ErrorCode.USER_ALREADY_EXIST);
        }

        repository.save(user);
        log.info("Created new user with ID: {}", user.getId());
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public User update(@NotNull User updatedUser) {
        repository.save(updatedUser);
        log.info("Updated user with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    @Transactional
    public User updatePerson(UserUpdateRequest request, String userId) {
        var user = findById(UUID.fromString(userId));

        Optional.ofNullable(request.firstName())
                .ifPresent(user::setFirstName);
        Optional.ofNullable(request.lastName())
                .ifPresent(user::setLastName);

        return update(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void bindPassword(String userId, BindPasswordRequest request) {
        var user = findById(UUID.fromString(userId));

        if (Objects.nonNull(user.getLocalAuth())) {
            throw new UserAlreadyExistsException("User already has password bound", ErrorCode.USER_ALREADY_PASSWORD_BOUND);
        }

        user.setLocalAuth(LocalAuth.builder()
                .user(user)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build());
        repository.save(user);
        passwordHistoryService.create(user, request.getPassword());
        log.info("User {} bound password", user.getEmail());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void changePassword(String userId, String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            throw new BadCredentialsException("New password must be different from old password");
        }

        var user = findById(UUID.fromString(userId));

        if (Objects.isNull(user.getLocalAuth())) {
            throw new IllegalStateException("User is not registered locally");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenticationCredentialsNotFoundException("Old password does not match");
        }

        passwordHistoryService.create(user, newPassword);

        user.getLocalAuth().setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
        log.info("Password changed for user with ID: {}", user.getId());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public User grantAdmin(UUID userId) {
        var user = findById(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new UserAlreadyExistsException("The user already has the admin role", ErrorCode.USER_ALREADY_ADMIN);
        }
        user.setRole(Role.ADMIN);
        var updateUser = update(user);
        refreshTokenService.revokeAllUserTokens(user.getId());
        return updateUser;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(UUID id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found", ErrorCode.USER_NOT_FOUND);
        }
        repository.deleteById(id);
        log.info("Deleted user with ID: {}", id);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByEmail(String email) {
        var user = findByEmail(email);
        repository.delete(user);
        log.info("Deleted user with email: {}", email);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void setUserBlocked(UUID userId, boolean blocked) {
        var user = findById(userId);
        user.setLocked(blocked);
        repository.save(user);
        log.info("User {} has been {}", userId, blocked ? "blocked" : "unblocked");
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
