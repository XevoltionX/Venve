package com.cs.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI bookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("预约服务 - Booking Service")
                        .description("预约创建/确认/取消 + Redis锁 + RabbitMQ延迟")
                        .version("1.0"));
    }
}
