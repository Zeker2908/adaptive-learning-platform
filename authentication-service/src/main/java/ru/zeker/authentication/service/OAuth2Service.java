package ru.zeker.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zeker.authentication.domain.dto.OAuth2UserInfo;
import ru.zeker.authentication.domain.mapper.UserMapper;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.domain.model.enums.OAuth2Provider;
import ru.zeker.authentication.exception.OAuth2ProviderException;
import ru.zeker.authentication.repository.UserRepository;

import java.util.Objects;

/**
 * Service for processing OAuth2 user authentication.
 * Responsible for registering new users or updating existing ones when logging in through third-party providers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Processes OAuth2 user login. Performs:
     * <ul>
     *     <li>Email validation and confirmation</li>
     *     <li>User information extraction</li>
     *     <li>New user registration or existing user update</li>
     * </ul>
     *
     * @param oAuth2User   User obtained from OAuth2 provider
     * @param providerName Provider name (e.g., "google", "github")
     * @return Registered or updated user
     * @throws OAuth2ProviderException  if email is not verified
     * @throws IllegalArgumentException if provider is unknown
     */
    @Transactional
    public User processOauth2User(OAuth2User oAuth2User, String providerName) {
        var emailVerified = (Boolean) oAuth2User.getAttribute("email_verified");
        var email = (String) oAuth2User.getAttribute("email");
        log.info("Processing OAuth2 user: email={}, emailVerified={}", email, emailVerified);

        if (Objects.isNull(emailVerified) || !emailVerified) {
            log.warn("OAuth2 authentication error: email address not verified for email={}", email);
            throw new OAuth2ProviderException("Email not verified");
        }

        var oAuth2Provider = extractProvider(providerName);
        var userInfo = oAuth2Provider.extractUserInfo(oAuth2User.getAttributes());

        return userRepository.findByEmail(email)
                .map(u -> update(u, userInfo, oAuth2Provider))
                .orElseGet(() -> register(userInfo, oAuth2Provider));
    }

    /**
     * Updates existing user by adding OAuth2 provider information,
     * if it was not previously saved.
     *
     * @param user           Existing user
     * @param userInfo       Information obtained from OAuth2 provider
     * @param oAuth2Provider OAuth2 provider
     * @return Updated user
     */
    private User update(User user, OAuth2UserInfo userInfo, OAuth2Provider oAuth2Provider) {
        if (Objects.isNull(user.getOauthAuth())) {
            userMapper.setOAuthAuth(user, userInfo, oAuth2Provider);
            log.info("Successfully added OAuth2 authentication to user");
            return userRepository.save(user);
        }
        return user;
    }

    /**
     * Registers a new user based on information obtained from OAuth2 provider.
     *
     * @param userInfo       User information
     * @param oAuth2Provider OAuth2 provider
     * @return New user
     */
    private User register(OAuth2UserInfo userInfo, OAuth2Provider oAuth2Provider) {
        var user = userMapper.toOAuthEntity(userInfo, oAuth2Provider);
        log.debug("Created new user object for OAuth2 registration");

        userRepository.save(user);
        log.info("Successfully registered OAuth2 user: id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    /**
     * Converts string provider name to corresponding {@link OAuth2Provider} enumeration.
     *
     * @param provider Provider name (e.g., "google")
     * @return {@link OAuth2Provider} enumeration value
     * @throws IllegalArgumentException if provider is unknown
     */
    private OAuth2Provider extractProvider(String provider) {
        try {
            return OAuth2Provider.valueOf(provider.toUpperCase());
        } catch (Exception e) {
            log.error("Failed to extract OAuth2Provider");
            throw new IllegalArgumentException("Failed to extract OAuth2Provider");
        }
    }
}
