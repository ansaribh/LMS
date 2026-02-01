package com.lms.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String authServerUrl;
    private String realm;
    private String clientId;
    private String clientSecret;

    public String getTokenUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public String getUserInfoUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
    }

    public String getLogoutUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
    }

    public String getCertsUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }
}
