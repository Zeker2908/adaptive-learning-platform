package ru.zeker.authentication.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.zeker.authentication.domain.model.enums.Role;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserResponse {
    protected UUID id;
    protected String email;
    protected String firstName;
    protected String lastName;
    protected Role role;
    protected boolean isLocalUser;
    protected boolean isOAuthUser;
    protected boolean isUserBlocked;
}
