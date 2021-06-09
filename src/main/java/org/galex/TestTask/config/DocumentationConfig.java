package org.galex.TestTask.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("TestTask API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .email("Gerasimov2122@icloud.com")
                                .name("Gerasimov Alexey")
                        )
        );
    }
}
