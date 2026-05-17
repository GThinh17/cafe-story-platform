package com.cafestory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cafeStoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CafeStory API")
                        .description("OpenAPI documentation for the CafeStory backend.")
                        .version("v1")
                        .license(new License().name("CafeStory")));
    }
}
