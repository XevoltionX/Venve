package com.cs.gateway.filter;

import com.cs.common.constant.AuthConstant;
import com.cs.common.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(AuthConstant.HEADER_TOKEN);
        if (authHeader == null || !authHeader.startsWith(AuthConstant.PREFIX_BEARER)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(AuthConstant.PREFIX_BEARER.length());
        if (!JwtUtil.validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Long userId = JwtUtil.getUserId(token);
        String userName = JwtUtil.getUserName(token);
        String role = JwtUtil.getRole(token);

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(AuthConstant.HEADER_USER_ID, String.valueOf(userId))
                .header(AuthConstant.HEADER_USER_NAME, userName)
                .header(AuthConstant.HEADER_USER_ROLE, role)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isWhiteListed(String path) {
        return Arrays.stream(AuthConstant.WHITE_LIST).anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
