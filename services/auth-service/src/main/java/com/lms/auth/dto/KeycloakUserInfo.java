package com.lms.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserInfo {

    private String sub;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    private String email;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String name;

    @JsonProperty("realm_access")
    private Map<String, Object> realmAccess;

    private List<String> roles;
}
