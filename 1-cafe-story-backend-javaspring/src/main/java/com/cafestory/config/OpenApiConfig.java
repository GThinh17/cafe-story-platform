package com.cafestory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI cafeStoryOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local backend"),
                        new Server().url("/").description("Current host")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken returned by /api/auth/login.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .info(new Info()
                        .title("CafeStory API")
                        .description("CafeStory backend API documentation. Login first, copy accessToken, then use Authorize with Bearer JWT to test protected APIs.")
                        .version("v1")
                        .license(new License().name("CafeStory")))
                .externalDocs(new ExternalDocumentation()
                        .description("Swagger UI")
                        .url("/swagger-ui.html"));
    }
}
