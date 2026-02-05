package ru.zeker.authentication.domain.component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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

    @Value("${app.admin.username}")
    private String adminName;

    /**
     * Initializes an administrator in the system.
     * If an administrator with the given email address does not exist, it creates an administrator with a generated password.
     * Logs information about the created administrator.
     *
     * @param args command line arguments
     */
    @Override
    @Transactional
    public void run(String... args) {
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

    /**
     * Generates a random password from {@value #CHARACTERS} with a length of {@value #STRING_LENGTH}.
     *
     * @return the generated password
     */
    private String generateRandomPassword() {
        var random = new SecureRandom();
        return random.ints(STRING_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
