package ru.zeker.authentication.domain.model.enums;

import ru.zeker.authentication.domain.dto.OAuth2UserInfo;

import java.util.Map;

public enum OAuth2Provider {
    GOOGLE{

        @Override
        public OAuth2UserInfo extractUserInfo(Map<String, Object> attributes) {
            return new OAuth2UserInfo(
                    (String) attributes.get("email"),
                    (String) attributes.get("sub"),
                    (String) attributes.get("given_name"),  // First name
                    (String) attributes.get("family_name")   // Last name
            );

        }
    };



    /**
     * Extracts user information from the specified OAuth2 attributes.
     *
     * @param attributes of attributes from the OAuth2 provider
     * @return the extracted user information
     */
    public abstract OAuth2UserInfo extractUserInfo(Map<String, Object> attributes);
}
