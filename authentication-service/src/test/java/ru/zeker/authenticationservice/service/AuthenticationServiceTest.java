package ru.zeker.authenticationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.zeker.authentication.domain.dto.Tokens;
import ru.zeker.authentication.domain.dto.request.LoginRequest;
import ru.zeker.authentication.domain.model.entity.User;
import ru.zeker.authentication.domain.model.enums.Role;
import ru.zeker.authentication.service.AuthenticationService;
import ru.zeker.authentication.service.JwtService;
import ru.zeker.authentication.service.RefreshTokenService;
import ru.zeker.authentication.service.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void login_success_returnsTokens() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("test@mail.com")
                .password("123456")
                .build();

        User user = User.builder()
                .email("test@mail.com")
                .role(Role.USER)
                .build();
        user.setId(UUID.randomUUID());

        Authentication authentication = mock(Authentication.class);

        Tokens expectedTokens = Tokens.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(user);

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

        // when
        Tokens result = authenticationService.login(request);

        // then
        assertThat(result.getToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(user);
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void login_shouldLowercaseEmail_beforeAuthentication() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("TEST@MAIL.COM")
                .password("123456")
                .build();

        Authentication authentication = mock(Authentication.class);
        User user = User.builder()
                .email("test@mail.com")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

        // when
        authenticationService.login(request);

        // then
        verify(authenticationManager).authenticate(
                argThat(auth ->
                        auth.getName().equals("test@mail.com")
                )
        );
    }
}