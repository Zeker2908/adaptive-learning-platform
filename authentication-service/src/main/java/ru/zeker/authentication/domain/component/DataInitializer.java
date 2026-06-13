package ru.zeker.authentication.domain.component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.zeker.authentication.config.DemoLoginProperties;
import ru.zeker.authentication.domain.dto.request.RegisterRequest;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.service.PasswordHistoryService;
import ru.zeker.authentication.service.UserService;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    private static final int STRING_LENGTH = 15;
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";

    private final UserService userService;
    private final PasswordHistoryService passwordHistoryService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DemoLoginProperties demoLoginProperties;

    @Value("${app.admin.username}")
    private String adminName;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initDemoUser();
    }

    private void initAdmin() {
        if (!userService.existsByEmail(adminName)) {
            final var password = generateRandomPassword();
            log.info("Creating an administrator with email: {}", adminName);
            var request = RegisterRequest.builder()
                    .email(adminName)
                    .password(password)
                    .firstName("ADMIN")
                    .build();
            var admin = userMapper.toAdmin(request, passwordEncoder);
            userService.create(admin);
            passwordHistoryService.create(admin, password);
            log.info("Administrator created");
            log.info(ANSI_GREEN + "Generated password: {}" + ANSI_RESET, password);
        } else {
            log.info("The administrator user has already been created.");
        }
    }

    private void initDemoUser() {
        if (!demoLoginProperties.isEnabled() || StringUtils.isBlank(demoLoginProperties.getEmail())) {
            return;
        }

        var email = demoLoginProperties.getEmail().toLowerCase();

        if (userService.existsByEmail(email)) {
            var user = userService.findByEmail(email);
            var localAuth = user.getLocalAuth();
            if (localAuth != null && Boolean.FALSE.equals(localAuth.getEnabled())) {
                localAuth.setEnabled(true);
                userService.update(user);
                log.info("Demo user {} activated for QR login", email);
            } else {
                log.info("Demo user {} already exists", email);
            }
            return;
        }

        final var password = generateRandomPassword();
        log.info("Creating demo user with email: {}", email);
        var request = RegisterRequest.builder()
                .email(email)
                .password(password)
                .firstName("Demo")
                .lastName("User")
                .build();
        var demoUser = userMapper.toEntity(request, passwordEncoder);
        demoUser.getLocalAuth().setEnabled(true);
        userService.create(demoUser);
        passwordHistoryService.create(demoUser, password);
        log.info("Demo user created for QR login");
        log.info(ANSI_GREEN + "Demo user password (login form): {}" + ANSI_RESET, password);
    }

    private String generateRandomPassword() {
        var random = new SecureRandom();
        return random.ints(STRING_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
