package com.lms.auth.service;

import com.lms.auth.entity.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Create a new user session
     */
    public UserSession createSession(UUID userId, String keycloakId, String username, String email,
                                      Set<String> roles, String accessToken, String refreshToken,
                                      String ipAddress, String userAgent, long expiresInSeconds) {
        String sessionId = UUID.randomUUID().toString();
        
        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .keycloakId(keycloakId)
                .username(username)
                .email(email)
                .roles(roles)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .active(true)
                .build();

        // Store session
        String sessionKey = SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(sessionKey, session, SESSION_TTL);

        // Add to user's sessions set
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, SESSION_TTL.toMinutes(), TimeUnit.MINUTES);

        log.info("Session created for user {}: {}", username, sessionId);
        return session;
    }

    /**
     * Get session by ID
     */
    public Optional<UserSession> getSession(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        Object session = redisTemplate.opsForValue().get(sessionKey);
        if (session instanceof UserSession userSession) {
            return Optional.of(userSession);
        }
        return Optional.empty();
    }

    /**
     * Update session with new tokens
     */
    public void updateSession(String sessionId, String accessToken, String refreshToken, long expiresInSeconds) {
        String sessionKey = SESSION_PREFIX + sessionId;
        Object existing = redisTemplate.opsForValue().get(sessionKey);
        
        if (existing instanceof UserSession session) {
            session.setAccessToken(accessToken);
            session.setRefreshToken(refreshToken);
            session.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds));
            redisTemplate.opsForValue().set(sessionKey, session, SESSION_TTL);
            log.debug("Session updated: {}", sessionId);
        }
    }

    /**
     * Invalidate a specific session
     */
    public void invalidateSession(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        Object session = redisTemplate.opsForValue().get(sessionKey);
        
        if (session instanceof UserSession userSession) {
            // Remove from user's sessions set
            String userSessionsKey = USER_SESSIONS_PREFIX + userSession.getUserId();
            redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        }
        
        redisTemplate.delete(sessionKey);
        log.info("Session invalidated: {}", sessionId);
    }

    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllUserSessions(UUID userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionIds != null) {
            for (Object sessionId : sessionIds) {
                redisTemplate.delete(SESSION_PREFIX + sessionId);
            }
        }
        
        redisTemplate.delete(userSessionsKey);
        log.info("All sessions invalidated for user: {}", userId);
    }

    /**
     * Check if session is valid
     */
    public boolean isSessionValid(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * Get active session count for a user
     */
    public long getActiveSessionCount(UUID userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Long count = redisTemplate.opsForSet().size(userSessionsKey);
        return count != null ? count : 0;
    }
}
