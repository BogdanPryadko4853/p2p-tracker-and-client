package com.bogdan.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI(GitProperties gitProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title("Tracker service api")
                        .description("api for torrent client")
                        .version(gitProperties.get("build.version"))
                        .contact(new Contact()
                                .name("API Support")
                                .email("bogdanpryadko1@gmail.com")));
    }
}
