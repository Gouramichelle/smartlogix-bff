package com.smartlogix.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todas las rutas de tu API
                        .allowedOrigins("http://localhost:3000", "http://localhost:5173") // Los puertos típicos de React
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite todos estos métodos
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}