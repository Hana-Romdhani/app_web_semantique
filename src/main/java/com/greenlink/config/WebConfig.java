package com.greenlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/ontology/**") // Adjust the path as needed
                .allowedOrigins("http://localhost:8080") // Replace with Laravel URL
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
