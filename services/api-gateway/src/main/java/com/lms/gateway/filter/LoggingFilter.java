package com.lms.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate or extract request ID
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Add request ID to response headers
        final String finalRequestId = requestId;
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, finalRequestId)
                .build();

        // Store start time for duration calculation
        exchange.getAttributes().put(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        log.info("Request: {} {} - RequestID: {} - Client: {}",
                request.getMethod(),
                request.getURI().getPath(),
                finalRequestId,
                request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown");

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(START_TIME_ATTRIBUTE);
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // Add request ID to response
                    response.getHeaders().add(REQUEST_ID_HEADER, finalRequestId);
                    
                    long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
                    
                    log.info("Response: {} {} - Status: {} - Duration: {}ms - RequestID: {}",
                            request.getMethod(),
                            request.getURI().getPath(),
                            response.getStatusCode(),
                            duration,
                            finalRequestId);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
