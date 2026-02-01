package com.lms.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    if (securityContext.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        var jwt = jwtAuth.getToken();
                        
                        // Extract user information from JWT
                        String userId = jwt.getClaimAsString("sub");
                        String userName = jwt.getClaimAsString("preferred_username");
                        String userEmail = jwt.getClaimAsString("email");
                        String roles = String.join(",", jwtAuth.getAuthorities().stream()
                                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                                .toList());

                        // Forward user information to downstream services
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header(USER_ID_HEADER, userId != null ? userId : "")
                                .header(USER_NAME_HEADER, userName != null ? userName : "")
                                .header(USER_EMAIL_HEADER, userEmail != null ? userEmail : "")
                                .header(USER_ROLES_HEADER, roles)
                                .build();

                        log.debug("Forwarding user info - ID: {}, Username: {}, Roles: {}", 
                                userId, userName, roles);

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
