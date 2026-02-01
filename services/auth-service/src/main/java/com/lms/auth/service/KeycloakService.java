package com.lms.auth.service;

import com.lms.auth.config.KeycloakProperties;
import com.lms.auth.dto.KeycloakTokenResponse;
import com.lms.auth.dto.KeycloakUserInfo;
import com.lms.common.exception.BadRequestException;
import com.lms.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    /**
     * Authenticate user with Keycloak and get tokens
     */
    public Mono<KeycloakTokenResponse> login(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());
        formData.add("username", username);
        formData.add("password", password);
        formData.add("scope", "openid profile email");

        return webClient.post()
                .uri(keycloakProperties.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .doOnSuccess(response -> log.info("User {} authenticated successfully", username))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error("Authentication failed for user {}: {}", username, ex.getMessage());
                    if (ex.getStatusCode().value() == 401) {
                        return new UnauthorizedException("Invalid username or password");
                    }
                    return new BadRequestException("Authentication failed: " + ex.getMessage());
                });
    }

    /**
     * Refresh access token using refresh token
     */
    public Mono<KeycloakTokenResponse> refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());
        formData.add("refresh_token", refreshToken);

        return webClient.post()
                .uri(keycloakProperties.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .doOnSuccess(response -> log.debug("Token refreshed successfully"))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error("Token refresh failed: {}", ex.getMessage());
                    if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                        return new UnauthorizedException("Invalid or expired refresh token");
                    }
                    return new BadRequestException("Token refresh failed: " + ex.getMessage());
                });
    }

    /**
     * Get user info from Keycloak using access token
     */
    public Mono<KeycloakUserInfo> getUserInfo(String accessToken) {
        return webClient.get()
                .uri(keycloakProperties.getUserInfoUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KeycloakUserInfo.class)
                .doOnSuccess(userInfo -> log.debug("User info retrieved for: {}", userInfo.getPreferredUsername()))
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error("Failed to get user info: {}", ex.getMessage());
                    return new UnauthorizedException("Failed to get user info");
                });
    }

    /**
     * Logout user from Keycloak (invalidate tokens)
     */
    public Mono<Void> logout(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());
        formData.add("refresh_token", refreshToken);

        return webClient.post()
                .uri(keycloakProperties.getLogoutUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("User logged out successfully"))
                .onErrorResume(ex -> {
                    log.warn("Logout request failed, but proceeding: {}", ex.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Initiate password reset for user
     */
    public Mono<Void> initiatePasswordReset(String email) {
        // This would typically be done via Keycloak Admin API
        // For simplicity, we'll just log the request
        log.info("Password reset initiated for email: {}", email);
        return Mono.empty();
    }
}
