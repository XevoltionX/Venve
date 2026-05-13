package com.cs.venue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI venueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("场馆服务 - Venue Service")
                        .description("球台列表、场馆查询")
                        .version("1.0"));
    }
}
