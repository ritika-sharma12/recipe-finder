package com.recipefinder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recipeFinderOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Recipe Finder API")
                .version("1.0.0")
                .description("Find recipes based on available ingredients and recipe filters.")
                .contact(new Contact().name("Recipe Finder Team")));
    }
}
