package com.lms.auth.service;

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

import java.util.HashSet;
import java.util.List;
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

        // Get user info from Keycloak
        KeycloakUserInfo userInfo = keycloakService.getUserInfo(tokenResponse.getAccessToken()).block();
        if (userInfo == null) {
            throw new UnauthorizedException("Failed to get user information");
        }

        // Create or update user profile
        UserProfile userProfile = getOrCreateUserProfile(userInfo);
        userProfile.recordLogin();
        userProfileRepository.save(userProfile);

        // Extract roles from Keycloak
        Set<String> roles = extractRoles(userInfo);

        // Create session
        UserSession session = sessionService.createSession(
                userProfile.getId(),
                userInfo.getSub(),
                userInfo.getPreferredUsername(),
                userInfo.getEmail(),
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
