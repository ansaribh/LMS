package com.lms.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Get the current authenticated user's JWT
     */
    public static Optional<Jwt> getCurrentUserJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }

    /**
     * Get the current authenticated user's Keycloak ID (subject)
     */
    public static Optional<String> getCurrentUserKeycloakId() {
        return getCurrentUserJwt().map(Jwt::getSubject);
    }

    /**
     * Get the current authenticated user's username
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserJwt()
                .map(jwt -> jwt.getClaimAsString("preferred_username"));
    }

    /**
     * Get the current authenticated user's email
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserJwt()
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    /**
     * Get the current authenticated user's ID from custom claim
     */
    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUserJwt()
                .map(jwt -> jwt.getClaimAsString("user_id"))
                .filter(id -> id != null && !id.isEmpty())
                .map(UUID::fromString);
    }

    /**
     * Get the current authenticated user's roles
     */
    public static Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Check if current user has a specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role.toUpperCase());
    }

    /**
     * Check if current user is an admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is an instructor
     */
    public static boolean isInstructor() {
        return hasRole("INSTRUCTOR");
    }

    /**
     * Check if current user is a student
     */
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * Check if current user is a parent
     */
    public static boolean isParent() {
        return hasRole("PARENT");
    }

    /**
     * Check if the user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
