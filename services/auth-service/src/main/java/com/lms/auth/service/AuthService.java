package com.lms.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.auth.dto.*;
import com.lms.auth.entity.UserProfile;
import com.lms.auth.entity.UserSession;
import com.lms.auth.repository.UserProfileRepository;
import com.lms.common.dto.auth.LoginRequest;
import com.lms.common.dto.auth.LoginResponse;
import com.lms.common.dto.auth.RefreshTokenRequest;
import com.lms.common.dto.auth.TokenResponse;
import com.lms.common.dto.UserDto;
import com.lms.common.enums.UserRole;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final SessionService sessionService;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Authenticate with Keycloak
        KeycloakTokenResponse tokenResponse = keycloakService.login(
                request.getUsername(), 
                request.getPassword()
        ).block();

        if (tokenResponse == null) {
            throw new UnauthorizedException("Authentication failed");
        }

        // Extract user info from JWT token
        Map<String, Object> tokenClaims = decodeJwtPayload(tokenResponse.getAccessToken());
        String sub = (String) tokenClaims.get("sub");
        String preferredUsername = (String) tokenClaims.getOrDefault("preferred_username", request.getUsername());
        String email = (String) tokenClaims.getOrDefault("email", request.getUsername() + "@lms.local");

        // Create or update user profile
        UserProfile userProfile = getOrCreateUserProfileFromToken(sub, preferredUsername, email);
        userProfile.recordLogin();
        userProfileRepository.save(userProfile);

        // Extract roles from token
        Set<String> roles = extractRolesFromToken(tokenClaims);

        // Create session
        UserSession session = sessionService.createSession(
                userProfile.getId(),
                sub,
                preferredUsername,
                email,
                roles,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                ipAddress,
                userAgent,
                tokenResponse.getExpiresIn()
        );

        // Build response
        UserDto userDto = mapToUserDto(userProfile, roles);

        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(tokenResponse.getExpiresIn())
                .user(userDto)
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid token format");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("Failed to decode JWT: {}", e.getMessage());
            throw new UnauthorizedException("Failed to decode token");
        }
    }
    
    private UserProfile getOrCreateUserProfileFromToken(String keycloakId, String username, String email) {
        return userProfileRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .keycloakId(keycloakId)
                            .username(username)
                            .email(email)
                            .active(true)
                            .build();
                    log.info("Creating new user profile for: {}", username);
                    return userProfileRepository.save(newProfile);
                });
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> extractRolesFromToken(Map<String, Object> claims) {
        Set<String> roles = new HashSet<>();
        
        // Check realm_access.roles
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                roles.addAll(realmRoles);
            }
        }
        
        // Check direct roles claim (from lms-scope mapper)
        Object directRoles = claims.get("roles");
        if (directRoles instanceof List<?>) {
            for (Object role : (List<?>) directRoles) {
                if (role instanceof String) {
                    roles.add((String) role);
                }
            }
        }
        
        // Ensure at least STUDENT role
        if (roles.isEmpty()) {
            roles.add("STUDENT");
        }
        
        return roles;
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        KeycloakTokenResponse tokenResponse = keycloakService.refreshToken(
                request.getRefreshToken()
        ).block();

        if (tokenResponse == null) {
            throw new UnauthorizedException("Token refresh failed");
        }

        return TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(tokenResponse.getExpiresIn())
                .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                .build();
    }

    public void logout(String refreshToken, String sessionId) {
        // Logout from Keycloak
        keycloakService.logout(refreshToken).block();

        // Invalidate local session if session ID is provided
        if (sessionId != null && !sessionId.isEmpty()) {
            sessionService.invalidateSession(sessionId);
        }

        log.info("User logged out successfully");
    }

    public UserDto getCurrentUser(String keycloakId) {
        UserProfile userProfile = userProfileRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));

        // Get roles from a session or default
        Set<String> roles = Set.of("STUDENT"); // Default role
        
        return mapToUserDto(userProfile, roles);
    }

    public void initiatePasswordReset(String email) {
        // Verify email exists
        if (!userProfileRepository.existsByEmail(email)) {
            // Don't reveal if email exists or not for security
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        keycloakService.initiatePasswordReset(email).block();
        log.info("Password reset initiated for: {}", email);
    }

    private UserProfile getOrCreateUserProfile(KeycloakUserInfo userInfo) {
        return userProfileRepository.findByKeycloakId(userInfo.getSub())
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .keycloakId(userInfo.getSub())
                            .username(userInfo.getPreferredUsername())
                            .email(userInfo.getEmail())
                            .firstName(userInfo.getGivenName())
                            .lastName(userInfo.getFamilyName())
                            .emailVerified(userInfo.isEmailVerified())
                            .active(true)
                            .build();
                    log.info("Creating new user profile for: {}", userInfo.getPreferredUsername());
                    return userProfileRepository.save(newProfile);
                });
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(KeycloakUserInfo userInfo) {
        Set<String> roles = new HashSet<>();
        
        if (userInfo.getRealmAccess() != null) {
            Object rolesObj = userInfo.getRealmAccess().get("roles");
            if (rolesObj instanceof List<?> rolesList) {
                roles.addAll((List<String>) rolesList);
            }
        }
        
        if (userInfo.getRoles() != null) {
            roles.addAll(userInfo.getRoles());
        }
        
        // Ensure at least STUDENT role
        if (roles.isEmpty()) {
            roles.add("STUDENT");
        }
        
        return roles;
    }

    private UserDto mapToUserDto(UserProfile profile, Set<String> roleStrings) {
        Set<UserRole> roles = roleStrings.stream()
                .map(role -> {
                    try {
                        return UserRole.valueOf(role.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(profile.getId())
                .keycloakId(profile.getKeycloakId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phone(profile.getPhone())
                .avatarUrl(profile.getAvatarUrl())
                .roles(roles)
                .active(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
