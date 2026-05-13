package com.cs.gateway.metrics;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MetricsFilter implements GlobalFilter, Ordered {

    private final MetricsCollector collector;

    public MetricsFilter(MetricsCollector collector) {
        this.collector = collector;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String route = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange).doFinally(signalType -> {
            long latency = System.currentTimeMillis() - start;
            boolean error = exchange.getResponse().getStatusCode() != null
                    && exchange.getResponse().getStatusCode().isError();
            collector.record(route, latency, error);
        });
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
